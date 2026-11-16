package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.CitizenReport
import com.example.data.model.SosEvent
import com.example.data.model.TrustedContact
import com.example.data.model.UserFeedback
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // ==================== USER PROFILE ====================
    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else null
            }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        email: String?
    ): Result<UserProfile> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserProfile(id = userId, userId = userId, displayName = displayName, email = email)
                Result.success(existing)
            } else {
                val safeName = displayName.ifBlank { "Guardian User" }.take(80)
                val data = buildMap<String, Any> {
                    put("userId", userId)
                    put("displayName", safeName)
                    if (!email.isNullOrBlank()) put("email", email.take(120))
                    put("preferredLanguage", "en")
                    put("selectedCity", "Puducherry")
                    put("shakeToSosEnabled", false)
                    put("customSosMessage", "EMERGENCY SOS: I need immediate help. Please check my location.")
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(data).await()
                Result.success(
                    UserProfile(
                        id = userId,
                        userId = userId,
                        displayName = safeName,
                        email = email
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserPreferences(
        userId: String,
        preferredLanguage: String? = null,
        selectedCity: String? = null,
        shakeToSosEnabled: Boolean? = null,
        customSosMessage: String? = null
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val updates = buildMap<String, Any> {
                preferredLanguage?.let { put("preferredLanguage", it) }
                selectedCity?.let { put("selectedCity", it.take(80)) }
                shakeToSosEnabled?.let { put("shakeToSosEnabled", it) }
                customSosMessage?.let { put("customSosMessage", it.take(280)) }
                put("updatedAt", FieldValue.serverTimestamp())
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== TRUSTED CONTACTS ====================
    fun observeTrustedContacts(userId: String): Flow<List<TrustedContact>> =
        db.collection("users").document(userId)
            .collection("trusted_contacts")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    TrustedContact::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedWith(compareByDescending<TrustedContact> { it.isPrimary }.thenBy { it.priority })
            }

    suspend fun addTrustedContact(
        userId: String,
        name: String,
        phone: String,
        relationship: String,
        priority: Int,
        isPrimary: Boolean
    ): Result<String> {
        val contactId = "c_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(userId)
            .collection("trusted_contacts").document(contactId)
        return try {
            val contactMap = mapOf(
                "userId" to userId,
                "name" to name.trim().take(80),
                "phone" to phone.trim().take(25),
                "relationship" to relationship.trim().ifBlank { "Family" }.take(40),
                "priority" to priority.coerceIn(1, 10).toLong(),
                "isPrimary" to isPrimary,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(contactMap).await()
            Result.success(contactId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateTrustedContact(
        userId: String,
        contact: TrustedContact
    ): Result<Unit> {
        require(contact.id.isNotBlank()) { "Contact ID must not be blank" }
        val docRef = db.collection("users").document(userId)
            .collection("trusted_contacts").document(contact.id)
        return try {
            val updates = mapOf(
                "name" to contact.name.trim().take(80),
                "phone" to contact.phone.trim().take(25),
                "relationship" to contact.relationship.trim().ifBlank { "Family" }.take(40),
                "priority" to contact.priority.coerceIn(1L, 10L),
                "isPrimary" to contact.isPrimary,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTrustedContact(userId: String, contactId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
            .collection("trusted_contacts").document(contactId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== SOS EVENTS ====================
    fun observeSosEvents(userId: String): Flow<List<SosEvent>> =
        db.collection("users").document(userId)
            .collection("sos_events")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SosEvent::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    suspend fun createSosEvent(
        userId: String,
        status: String,
        triggerType: String,
        lat: Double,
        lon: Double,
        accuracyMeters: Double,
        locationLabel: String,
        message: String,
        contactIds: List<String>,
        deliveryNote: String
    ): Result<String> {
        val eventId = "sos_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(userId)
            .collection("sos_events").document(eventId)
        return try {
            val event = SosEvent(
                userId = userId,
                status = status,
                triggerType = triggerType,
                lat = lat.coerceIn(-90.0, 90.0),
                lon = lon.coerceIn(-180.0, 180.0),
                accuracyMeters = accuracyMeters.coerceIn(0.0, 100000.0),
                locationLabel = locationLabel.take(120),
                message = message.take(320),
                contactIds = contactIds.take(10),
                deliveryNote = deliveryNote.take(240)
            )
            docRef.set(event).await()
            Result.success(eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateSosEventStatus(
        userId: String,
        eventId: String,
        newStatus: String,
        deliveryNote: String,
        lat: Double? = null,
        lon: Double? = null,
        accuracyMeters: Double? = null,
        locationLabel: String? = null
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
            .collection("sos_events").document(eventId)
        return try {
            val updates = buildMap<String, Any> {
                put("status", newStatus)
                put("deliveryNote", deliveryNote.take(240))
                lat?.let { put("lat", it.coerceIn(-90.0, 90.0)) }
                lon?.let { put("lon", it.coerceIn(-180.0, 180.0)) }
                accuracyMeters?.let { put("accuracyMeters", it.coerceIn(0.0, 100000.0)) }
                locationLabel?.let { put("locationLabel", it.take(120)) }
                put("updatedAt", FieldValue.serverTimestamp())
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== CITIZEN REPORTS ====================
    fun observeCitizenReports(userId: String): Flow<List<CitizenReport>> =
        db.collection("users").document(userId)
            .collection("citizen_reports")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CitizenReport::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    suspend fun createCitizenReport(
        userId: String,
        category: String,
        description: String,
        lat: Double,
        lon: Double,
        nearestStation: String,
        severity: Int
    ): Result<String> {
        val reportId = "rep_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(userId)
            .collection("citizen_reports").document(reportId)
        return try {
            val report = CitizenReport(
                userId = userId,
                category = category.take(60),
                description = description.take(600),
                lat = lat.coerceIn(-90.0, 90.0),
                lon = lon.coerceIn(-180.0, 180.0),
                nearestStation = nearestStation.take(80),
                severity = severity.coerceIn(1, 5).toLong(),
                status = "received"
            )
            docRef.set(report).await()
            Result.success(reportId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun acknowledgeCitizenReport(userId: String, reportId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId)
            .collection("citizen_reports").document(reportId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "acknowledged",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== FEEDBACK ====================
    suspend fun submitFeedback(userId: String, message: String): Result<String> {
        val feedbackId = "fb_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(userId)
            .collection("feedback").document(feedbackId)
        return try {
            val feedback = UserFeedback(
                userId = userId,
                message = message.trim().take(800)
            )
            docRef.set(feedback).await()
            Result.success(feedbackId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
