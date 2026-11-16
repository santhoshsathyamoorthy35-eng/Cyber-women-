package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "GoogleAuthManager"

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

sealed interface AuthActionResult {
    data class Success(val user: FirebaseUser) : AuthActionResult
    data object Cancelled : AuthActionResult
    data object NoSavedCredential : AuthActionResult
    data class Error(val message: String) : AuthActionResult
}

object GoogleAuthManager {

    suspend fun trySilentGoogleSignIn(activityContext: Context): AuthActionResult {
        return try {
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            if (webClientId.isBlank()) {
                return AuthActionResult.Error("Missing Google OAuth web client ID in resources.")
            }
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            handleCredential(result.credential)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            AuthActionResult.Cancelled
        } catch (e: NoCredentialException) {
            AuthActionResult.NoSavedCredential
        } catch (e: Exception) {
            Log.w(TAG, "Silent Google Sign-In unavailable: ${e.message}")
            AuthActionResult.NoSavedCredential
        }
    }

    suspend fun signInWithGoogleButton(activityContext: Context): AuthActionResult {
        return try {
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            if (webClientId.isBlank()) {
                return AuthActionResult.Error("Google Sign-In configuration missing.")
            }
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            handleCredential(result.credential)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            AuthActionResult.Cancelled
        } catch (e: NoCredentialException) {
            AuthActionResult.Error("No Google account found on device. Please add a Google account in device settings.")
        } catch (e: Exception) {
            Log.e(TAG, "Interactive Google Sign-In failed", e)
            AuthActionResult.Error(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }

    private suspend fun handleCredential(credential: androidx.credentials.Credential): AuthActionResult {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            return if (user != null) {
                AuthActionResult.Success(user)
            } else {
                AuthActionResult.Error("Authentication completed without user record.")
            }
        }
        return AuthActionResult.Error("Unsupported credential type returned.")
    }

    suspend fun signOut(activityContext: Context) {
        withContext(NonCancellable) {
            Firebase.auth.signOut()
            try {
                CredentialManager.create(activityContext)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "ClearCredentialState failed", e)
            }
        }
    }
}
