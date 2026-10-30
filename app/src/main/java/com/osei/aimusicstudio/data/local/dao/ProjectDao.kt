package com.osei.aimusicstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity
import com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) providing unified insert, query, update, and delete
 * operations for both [LocalAudioProjectEntity] (AudioProject) and
 * [LocalVideoProjectEntity] (VideoProject) entities in Room.
 */
@Dao
interface ProjectDao {

    // ==========================================
    // AUDIO PROJECT OPERATIONS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioProject(project: LocalAudioProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioProjects(projects: List<LocalAudioProjectEntity>)

    @Update
    suspend fun updateAudioProject(project: LocalAudioProjectEntity)

    @Query("SELECT * FROM local_audio_projects WHERE userId = :userId ORDER BY createdAtEpoch DESC")
    fun observeAudioProjects(userId: String): Flow<List<LocalAudioProjectEntity>>

    @Query("SELECT * FROM local_audio_projects WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getAudioProject(userId: String, id: String): LocalAudioProjectEntity?

    @Query("SELECT * FROM local_audio_projects WHERE id = :id LIMIT 1")
    suspend fun getAudioProjectById(id: String): LocalAudioProjectEntity?

    @Query("SELECT * FROM local_audio_projects WHERE userId = :userId AND status = :status ORDER BY createdAtEpoch DESC")
    fun observeAudioProjectsByStatus(userId: String, status: String): Flow<List<LocalAudioProjectEntity>>

    @Query("SELECT * FROM local_audio_projects WHERE userId = :userId")
    suspend fun getAudioProjectsByUser(userId: String): List<LocalAudioProjectEntity>

    @Query("DELETE FROM local_audio_projects WHERE userId = :userId AND id = :id")
    suspend fun deleteAudioProject(userId: String, id: String)

    @Query("DELETE FROM local_audio_projects WHERE id = :id")
    suspend fun deleteAudioProjectById(id: String)

    @Delete
    suspend fun deleteAudioProject(project: LocalAudioProjectEntity)

    @Query("DELETE FROM local_audio_projects WHERE userId = :userId")
    suspend fun clearAudioProjects(userId: String)


    // ==========================================
    // VIDEO PROJECT OPERATIONS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoProject(project: LocalVideoProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoProjects(projects: List<LocalVideoProjectEntity>)

    @Update
    suspend fun updateVideoProject(project: LocalVideoProjectEntity)

    @Query("SELECT * FROM local_video_projects WHERE userId = :userId ORDER BY createdAtEpoch DESC")
    fun observeVideoProjects(userId: String): Flow<List<LocalVideoProjectEntity>>

    @Query("SELECT * FROM local_video_projects WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getVideoProject(userId: String, id: String): LocalVideoProjectEntity?

    @Query("SELECT * FROM local_video_projects WHERE id = :id LIMIT 1")
    suspend fun getVideoProjectById(id: String): LocalVideoProjectEntity?

    @Query("SELECT * FROM local_video_projects WHERE userId = :userId AND status = :status ORDER BY createdAtEpoch DESC")
    fun observeVideoProjectsByStatus(userId: String, status: String): Flow<List<LocalVideoProjectEntity>>

    @Query("SELECT * FROM local_video_projects WHERE userId = :userId")
    suspend fun getVideoProjectsByUser(userId: String): List<LocalVideoProjectEntity>

    @Query("DELETE FROM local_video_projects WHERE userId = :userId AND id = :id")
    suspend fun deleteVideoProject(userId: String, id: String)

    @Query("DELETE FROM local_video_projects WHERE id = :id")
    suspend fun deleteVideoProjectById(id: String)

    @Delete
    suspend fun deleteVideoProject(project: LocalVideoProjectEntity)

    @Query("DELETE FROM local_video_projects WHERE userId = :userId")
    suspend fun clearVideoProjects(userId: String)
}
