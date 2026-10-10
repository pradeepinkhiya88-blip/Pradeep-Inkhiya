package com.example.data.repository

import com.example.data.api.GeminiClipService
import com.example.data.api.YouTubeHelper
import com.example.data.local.ClipDao
import com.example.data.local.SavedProjectEntity
import com.example.data.model.ClipDurationTarget
import com.example.data.model.ClipItem
import com.example.data.model.ClipObjective
import com.example.data.model.VideoClipProject
import com.example.data.model.YouTubeVideoInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClipRepository(
    private val clipDao: ClipDao,
    private val geminiService: GeminiClipService = GeminiClipService()
) {

    val savedProjectsFlow: Flow<List<VideoClipProject>> = clipDao.getAllProjectsFlow().map { entities ->
        entities.map { it.toVideoClipProject() }
    }

    suspend fun resolveVideo(urlOrId: String): YouTubeVideoInfo {
        return YouTubeHelper.fetchVideoInfo(urlOrId)
    }

    suspend fun generateClipsForVideo(
        videoInfo: YouTubeVideoInfo,
        objective: ClipObjective,
        durationTarget: ClipDurationTarget
    ): VideoClipProject {
        val clips = geminiService.generateClips(videoInfo, objective, durationTarget)
        val project = VideoClipProject(
            videoInfo = videoInfo,
            objective = objective,
            clips = clips
        )
        // Auto-save project to history
        clipDao.insertOrUpdate(SavedProjectEntity.fromProject(project))
        return project
    }

    suspend fun toggleFavorite(project: VideoClipProject, clipId: String): VideoClipProject {
        val updatedClips = project.clips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(isFavorite = !clip.isFavorite)
            } else clip
        }
        val updatedProject = project.copy(clips = updatedClips)
        clipDao.insertOrUpdate(SavedProjectEntity.fromProject(updatedProject))
        return updatedProject
    }

    suspend fun updateClipTimestamps(
        project: VideoClipProject,
        clipId: String,
        newStart: Int,
        newEnd: Int,
        newTitle: String
    ): VideoClipProject {
        val updatedClips = project.clips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(
                    title = newTitle,
                    startSeconds = newStart.coerceAtLeast(0),
                    endSeconds = newEnd.coerceAtLeast(newStart + 3)
                )
            } else clip
        }
        val updatedProject = project.copy(clips = updatedClips)
        clipDao.insertOrUpdate(SavedProjectEntity.fromProject(updatedProject))
        return updatedProject
    }

    suspend fun deleteProject(projectId: String) {
        clipDao.deleteById(projectId)
    }
}
