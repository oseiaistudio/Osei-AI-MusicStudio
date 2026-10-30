package com.osei.aimusicstudio.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.osei.aimusicstudio.auth.SecureAuthSession
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.osei.aimusicstudio.R
import com.osei.aimusicstudio.core.OperationType
import com.osei.aimusicstudio.core.handleFirestoreError
import com.osei.aimusicstudio.data.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface SongRepository {
    fun observeSongs(userId: String): Flow<List<Song>>
    suspend fun getSong(userId: String, songId: String): Song?
    suspend fun createSong(song: Song): Result<String>
    suspend fun updateSong(song: Song): Result<Unit>
    suspend fun deleteSong(userId: String, songId: String): Result<Unit>
    suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit>
}

class FirestoreSongRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val secureAuth: SecureAuthSession = SecureAuthSession(auth)
) : SongRepository {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private suspend fun requireVerifiedUserId(): String {
        return secureAuth.requireVerifiedUid()
    }

    override fun observeSongs(userId: String): Flow<List<Song>> = flow {
        val path = "users/$userId/songs"
        val query = db.collection("users").document(userId).collection("songs")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        query.snapshots()
            .map { snapshot -> snapshot.toObjects(Song::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    override suspend fun getSong(userId: String, songId: String): Song? {
        val path = "users/$userId/songs/$songId"
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("songs").document(songId).get().await()
            if (snapshot.exists()) snapshot.toObject(Song::class.java) else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    override suspend fun createSong(song: Song): Result<String> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val songId = if (song.id.isNotBlank()) song.id else "song_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "users/$uid/songs/$songId"
        val docRef = db.collection("users").document(uid).collection("songs").document(songId)

        return try {
            val payload = song.toMap() + mapOf(
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(songId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun updateSong(song: Song): Result<Unit> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        require(song.id.isNotBlank()) { "Song ID must not be blank for update" }
        val path = "users/$uid/songs/${song.id}"
        val docRef = db.collection("users").document(uid).collection("songs").document(song.id)

        return try {
            val payload = song.toMap() + mapOf(
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun deleteSong(userId: String, songId: String): Result<Unit> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/songs/$songId"
        val docRef = db.collection("users").document(uid).collection("songs").document(songId)

        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(userId: String, songId: String, currentFavorite: Boolean): Result<Unit> {
        val uid = try {
            requireVerifiedUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/songs/$songId"
        val docRef = db.collection("users").document(uid).collection("songs").document(songId)

        return try {
            docRef.update(
                mapOf(
                    "isFavorite" to !currentFavorite,
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
