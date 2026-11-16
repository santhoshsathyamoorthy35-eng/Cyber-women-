package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CyberWomenFirestoreRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun trustedContacts_ownerCrudAndRealtimeFlow_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreRepository(firestore)

        val profileRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile(uid, "Meera", ALICE_EMAIL)
        }
        assertTrue(profileRes.isSuccess)

        val contactId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addTrustedContact(
                userId = uid,
                name = "Amma",
                phone = "+919876543210",
                relationship = "Mother",
                priority = 1,
                isPrimary = true
            ).getOrThrow()
        }
        assertTrue(contactId.isNotBlank())

        val contacts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTrustedContacts(uid).first { list -> list.any { it.id == contactId } }
        }
        assertEquals(1, contacts.count { it.id == contactId })

        val delRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteTrustedContact(uid, contactId)
        }
        assertTrue(delRes.isSuccess)
    }

    @Test
    fun sosEvent_createAndTerminalCancel_enforcesRules() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreRepository(firestore)

        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createSosEvent(
                userId = uid,
                status = "created",
                triggerType = "hold",
                lat = 11.934,
                lon = 79.830,
                accuracyMeters = 15.0,
                locationLabel = "Goubert Market, Puducherry",
                message = "Need urgent help",
                contactIds = listOf("c_1"),
                deliveryNote = "Logged locally; call 112."
            ).getOrThrow()
        }

        val cancelRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateSosEventStatus(
                userId = uid,
                eventId = eventId,
                newStatus = "cancelled",
                deliveryNote = "Cancelled by user"
            )
        }
        assertTrue(cancelRes.isSuccess)

        // Attempt to reopen after terminal cancelled state must fail with PERMISSION_DENIED
        val reopenRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateSosEventStatus(
                userId = uid,
                eventId = eventId,
                newStatus = "logged_cloud",
                deliveryNote = "Should be blocked"
            )
        }
        assertTrue(reopenRes.isFailure)
        val fsEx = reopenRes.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun citizenReports_crossUserAndUnauthenticated_failWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreRepository(firestore)

        val reportId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCitizenReport(
                userId = aliceUid,
                category = "Unsafe area / poor lighting",
                description = "Dark alley behind bus stand.",
                lat = 11.937,
                lon = 79.826,
                nearestStation = "Grand Bazaar PS",
                severity = 4
            ).getOrThrow()
        }

        val ackRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.acknowledgeCitizenReport(aliceUid, reportId)
        }
        assertTrue(ackRes.isSuccess)

        // Bob tries to list Alice's reports
        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreRepository(firestore)
        var caughtBob: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeCitizenReports(aliceUid).first()
            }
        } catch (t: Throwable) {
            caughtBob = t
        }
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            caughtBob?.unwrapFirestoreException()?.code
        )

        // Unauthenticated caller fails
        auth.signOut()
        var caughtUnauth: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                aliceRepo.observeTrustedContacts(aliceUid).first()
            }
        } catch (t: Throwable) {
            caughtUnauth = t
        }
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            caughtUnauth?.unwrapFirestoreException()?.code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice_cw@test.com"
        const val BOB_EMAIL = "bob_cw@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
