package com.osei.aimusicstudio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) providing insert, query, update, and delete
 * operations for [LocalVideoProjectEntity] (VideoProject) in Room.
 */
@Dao
interface VideoProjectDao {

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
