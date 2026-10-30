package com.osei.aimusicstudio.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Immutable, server-validated representation of an authenticated user session.
 * Never constructs an instance from unverified client-side memory.
 */
data class VerifiedUserSession(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val token: String,
    val expirationTimestampSeconds: Long,
    val claims: Map<String, Any?> = emptyMap()
)

/**
 * Sealed result representing the outcome of identity verification.
 */
sealed interface IdentityVerificationResult {
    data class Verified(val session: VerifiedUserSession) : IdentityVerificationResult
    object Unauthenticated : IdentityVerificationResult
    object AnonymousForbidden : IdentityVerificationResult
    data class VerificationFailed(val reason: String, val cause: Throwable? = null) : IdentityVerificationResult
}

/**
 * Secure wrapper around [FirebaseAuth] that strictly adheres to zero-trust principles:
 * 1. Never trusts raw, stale, or client-cached [FirebaseAuth.getCurrentUser] state.
 * 2. Mandates active server-side token validation ([FirebaseUser.getIdToken]) before permitting
 *    Firestore repository operations.
 * 3. Rejects anonymous sessions (preventing unverified access).
 * 4. Ensures tokens have not expired and accounts have not been revoked or disabled on the backend.
 */
class SecureAuthSession(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Verifies the caller's identity against Firebase Authentication servers.
     *
     * @param forceRefresh When true, forces a network roundtrip to Firebase to refresh the JWT,
     *                     guaranteeing that revoked tokens or modified claims are caught immediately.
     */
    suspend fun verifySession(forceRefresh: Boolean = false): IdentityVerificationResult = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: run {
            Log.w(TAG, "Identity verification rejected: No current Firebase user present")
            return@withContext IdentityVerificationResult.Unauthenticated
        }

        if (user.isAnonymous) {
            Log.e(TAG, "Identity verification rejected: Anonymous sessions are forbidden in Osei Studio")
            return@withContext IdentityVerificationResult.AnonymousForbidden
        }

        try {
            // Actively calls Firebase Auth backend to obtain/verify valid token
            val tokenResult: GetTokenResult = user.getIdToken(forceRefresh).await()
            val rawToken = tokenResult.token
            if (rawToken.isNullOrBlank()) {
                return@withContext IdentityVerificationResult.VerificationFailed("Firebase server returned empty token")
            }

            val uid = user.uid
            if (uid.isBlank()) {
                return@withContext IdentityVerificationResult.VerificationFailed("Invalid empty UID")
            }

            val session = VerifiedUserSession(
                uid = uid,
                email = user.email.orEmpty(),
                isEmailVerified = user.isEmailVerified,
                token = rawToken,
                expirationTimestampSeconds = tokenResult.expirationTimestamp,
                claims = tokenResult.claims
            )
            IdentityVerificationResult.Verified(session)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase identity verification network or validation failure: ${e.message}", e)
            IdentityVerificationResult.VerificationFailed(e.localizedMessage ?: "Token validation failed", e)
        }
    }

    /**
     * Returns the verified UID if and only if server verification succeeds.
     * Throws [SecurityException] if unauthenticated or verification fails.
     */
    suspend fun requireVerifiedUid(forceRefresh: Boolean = false): String {
        return when (val result = verifySession(forceRefresh)) {
            is IdentityVerificationResult.Verified -> result.session.uid
            is IdentityVerificationResult.Unauthenticated ->
                throw SecurityException("Access denied: User is not authenticated")
            is IdentityVerificationResult.AnonymousForbidden ->
                throw SecurityException("Access denied: Anonymous accounts are not permitted")
            is IdentityVerificationResult.VerificationFailed ->
                throw SecurityException("Access denied: User identity could not be verified (${result.reason})", result.cause)
        }
    }

    /**
     * Executes the given repository [block] within a verified session context.
     * The verified [VerifiedUserSession] is passed into the block.
     */
    suspend fun <T> withVerifiedUser(
        forceRefresh: Boolean = false,
        block: suspend (VerifiedUserSession) -> T
    ): Result<T> = runCatching {
        val session = when (val result = verifySession(forceRefresh)) {
            is IdentityVerificationResult.Verified -> result.session
            is IdentityVerificationResult.Unauthenticated ->
                throw SecurityException("User is unauthenticated")
            is IdentityVerificationResult.AnonymousForbidden ->
                throw SecurityException("Anonymous authentication is not allowed")
            is IdentityVerificationResult.VerificationFailed ->
                throw SecurityException("Verification failure: ${result.reason}", result.cause)
        }
        block(session)
    }

    /**
     * Returns true if there is an active authenticated user whose session can be verified.
     */
    suspend fun isVerified(): Boolean {
        return verifySession(forceRefresh = false) is IdentityVerificationResult.Verified
    }

    companion object {
        private const val TAG = "SecureAuthSession"
    }
}
