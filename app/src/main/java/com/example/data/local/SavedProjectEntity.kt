package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ClipItem
import com.example.data.model.ClipObjective
import com.example.data.model.VideoClipProject
import com.example.data.model.YouTubeVideoInfo
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "saved_projects")
data class SavedProjectEntity(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
    val originalUrl: String,
    val objectiveName: String,
    val clipsJson: String,
    val createdAt: Long
) {
    fun toVideoClipProject(): VideoClipProject {
        val videoInfo = YouTubeVideoInfo(
            videoId = videoId,
            title = title,
            authorName = authorName,
            thumbnailUrl = thumbnailUrl,
            originalUrl = originalUrl
        )

        val objective = try {
            ClipObjective.valueOf(objectiveName)
        } catch (_: Exception) {
            ClipObjective.VIRAL_SHORTS
        }

        val clipsList = mutableListOf<ClipItem>()
        try {
            val jsonArray = JSONArray(clipsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val hashtagsList = mutableListOf<String>()
                val tagsArr = obj.optJSONArray("hashtags")
                if (tagsArr != null) {
                    for (t in 0 until tagsArr.length()) {
                        hashtagsList.add(tagsArr.getString(t))
                    }
                }

                clipsList.add(
                    ClipItem(
                        id = obj.optString("id"),
                        videoId = obj.optString("videoId", videoId),
                        title = obj.optString("title"),
                        startSeconds = obj.optInt("startSeconds"),
                        endSeconds = obj.optInt("endSeconds"),
                        viralScore = obj.optInt("viralScore", 85),
                        hookScore = obj.optInt("hookScore", 85),
                        retentionScore = obj.optInt("retentionScore", 85),
                        emotionalScore = obj.optInt("emotionalScore", 85),
                        hookReason = obj.optString("hookReason"),
                        transcriptSnippet = obj.optString("transcriptSnippet"),
                        hashtags = hashtagsList,
                        aspectRatio = obj.optString("aspectRatio", "9:16 Shorts"),
                        callToAction = obj.optString("callToAction"),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                )
            }
        } catch (_: Exception) {}

        return VideoClipProject(
            id = id,
            videoInfo = videoInfo,
            objective = objective,
            clips = clipsList,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromProject(project: VideoClipProject): SavedProjectEntity {
            val jsonArray = JSONArray()
            for (clip in project.clips) {
                val obj = JSONObject()
                obj.put("id", clip.id)
                obj.put("videoId", clip.videoId)
                obj.put("title", clip.title)
                obj.put("startSeconds", clip.startSeconds)
                obj.put("endSeconds", clip.endSeconds)
                obj.put("viralScore", clip.viralScore)
                obj.put("hookScore", clip.hookScore)
                obj.put("retentionScore", clip.retentionScore)
                obj.put("emotionalScore", clip.emotionalScore)
                obj.put("hookReason", clip.hookReason)
                obj.put("transcriptSnippet", clip.transcriptSnippet)
                obj.put("aspectRatio", clip.aspectRatio)
                obj.put("callToAction", clip.callToAction)
                obj.put("isFavorite", clip.isFavorite)

                val tagsArr = JSONArray()
                for (tag in clip.hashtags) {
                    tagsArr.put(tag)
                }
                obj.put("hashtags", tagsArr)

                jsonArray.put(obj)
            }

            return SavedProjectEntity(
                id = project.id,
                videoId = project.videoInfo.videoId,
                title = project.videoInfo.title,
                authorName = project.videoInfo.authorName,
                thumbnailUrl = project.videoInfo.thumbnailUrl,
                originalUrl = project.videoInfo.originalUrl,
                objectiveName = project.objective.name,
                clipsJson = jsonArray.toString(),
                createdAt = project.createdAt
            )
        }
    }
}
