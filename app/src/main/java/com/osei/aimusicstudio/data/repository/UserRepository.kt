package com.osei.aimusicstudio.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.osei.aimusicstudio.auth.SecureAuthSession
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.osei.aimusicstudio.R
import com.osei.aimusicstudio.core.OperationType
import com.osei.aimusicstudio.core.handleFirestoreError
import com.osei.aimusicstudio.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface UserRepository {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun createOrUpdateProfile(profile: UserProfile): Result<Unit>
    suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit>
}

class FirestoreUserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val secureAuth: SecureAuthSession = SecureAuthSession(auth)
) : UserRepository {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private suspend fun requireVerifiedUserId(): String {
        return secureAuth.requireVerifiedUid()
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                throw e
            }
            .collect { emit(it) }
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    override suspend fun createOrUpdateProfile(profile: UserProfile): Result<Unit> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = profile.toMap() + mapOf(
                "userId" to uid,
                "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun updatePreferences(userId: String, language: String, genre: String): Result<Unit> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        if (uid != userId) {
            return Result.failure(SecurityException("Access denied: Cannot update preferences for another user"))
        }
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.update(
                mapOf(
                    "preferredLanguage" to language,
                    "preferredGenre" to genre,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
