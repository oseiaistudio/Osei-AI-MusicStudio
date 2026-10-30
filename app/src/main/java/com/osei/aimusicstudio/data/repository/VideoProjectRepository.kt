package com.osei.aimusicstudio.data.repository

import com.osei.aimusicstudio.data.local.dao.VideoProjectDao
import com.osei.aimusicstudio.data.local.mapper.toDomainModel
import com.osei.aimusicstudio.data.local.mapper.toDomainVideoModels
import com.osei.aimusicstudio.data.local.mapper.toLocalEntity
import com.osei.aimusicstudio.data.model.VideoProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface VideoProjectRepository {
    fun observeVideoProjects(userId: String): Flow<List<VideoProject>>
    suspend fun getVideoProject(userId: String, id: String): VideoProject?
    suspend fun createVideoProject(project: VideoProject): Result<String>
    suspend fun updateVideoProject(project: VideoProject): Result<Unit>
    suspend fun deleteVideoProject(userId: String, id: String): Result<Unit>
}

/**
 * Local-First Room implementation of [VideoProjectRepository].
 *
 * All video projects remain strictly on-device in Room (Budget GH₵0).
 * No remote calls, no fake video URLs, and no cloud sync conversion.
 */
class RoomVideoProjectRepository(
    private val videoProjectDao: VideoProjectDao
) : VideoProjectRepository {

    override fun observeVideoProjects(userId: String): Flow<List<VideoProject>> {
        return videoProjectDao.observeVideoProjects(userId).map { entities ->
            entities.toDomainVideoModels()
        }
    }

    override suspend fun getVideoProject(userId: String, id: String): VideoProject? {
        return videoProjectDao.getVideoProject(userId, id)?.toDomainModel()
    }

    override suspend fun createVideoProject(project: VideoProject): Result<String> {
        return try {
            val entity = project.toLocalEntity()
            videoProjectDao.insertVideoProject(entity)
            Result.success(project.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVideoProject(project: VideoProject): Result<Unit> {
        return try {
            val entity = project.toLocalEntity()
            videoProjectDao.updateVideoProject(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVideoProject(userId: String, id: String): Result<Unit> {
        return try {
            videoProjectDao.deleteVideoProject(userId, id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
