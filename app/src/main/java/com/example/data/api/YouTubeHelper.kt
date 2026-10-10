package com.example.data.api

import com.example.data.model.YouTubeVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.regex.Pattern

object YouTubeHelper {
    private val client = OkHttpClient()

    // Regex matching standard watch, shorts, live, embed, and youtu.be links
    private val YOUTUBE_ID_PATTERN = Pattern.compile(
        "(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|v/|embed/|shorts/|live/)|youtu\\.be/)([a-zA-Z0-9_-]{11})",
        Pattern.CASE_INSENSITIVE
    )

    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        val matcher = YOUTUBE_ID_PATTERN.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        // Direct 11-char ID check
        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }
        return null
    }

    suspend fun fetchVideoInfo(rawUrlOrId: String): YouTubeVideoInfo = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(rawUrlOrId)
            ?: throw IllegalArgumentException("Could not find a valid YouTube video ID in: $rawUrlOrId")

        val standardWatchUrl = "https://www.youtube.com/watch?v=$videoId"
        val oEmbedUrl = "https://www.youtube.com/oembed?url=$standardWatchUrl&format=json"

        var title = "YouTube Video ($videoId)"
        var author = "YouTube Creator"
        var thumbnail = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        try {
            val request = Request.Builder()
                .url(oEmbedUrl)
                .header("User-Agent", "Mozilla/5.0 AutoClipAI/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string().orEmpty()
                if (bodyStr.isNotEmpty()) {
                    val json = JSONObject(bodyStr)
                    title = json.optString("title", title)
                    author = json.optString("author_name", author)
                    val oembedThumb = json.optString("thumbnail_url", "")
                    if (oembedThumb.isNotEmpty()) {
                        thumbnail = oembedThumb
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully to default thumbnail and video ID title
        }

        YouTubeVideoInfo(
            videoId = videoId,
            title = title,
            authorName = author,
            thumbnailUrl = thumbnail,
            originalUrl = standardWatchUrl
        )
    }

    // Curated high quality presets for instant testing
    val SAMPLE_PRESETS = listOf(
        SampleVideo(
            title = "Steve Jobs: How to Live Before You Die",
            channel = "Stanford",
            videoId = "UF8uR6Z6KLc",
            description = "Stanford Commencement Address covering passion, love, loss & death."
        ),
        SampleVideo(
            title = "Sam Altman: OpenAI, AGI & Future of Humanity",
            channel = "Lex Fridman",
            videoId = "jvqFAi7vkBc",
            description = "Deep conversation on artificial general intelligence and the tech roadmap."
        ),
        SampleVideo(
            title = "MrBeast: $456,000 Squid Game in Real Life",
            channel = "MrBeast",
            videoId = "08l0C-26iI0",
            description = "Massive real-life recreation with intense hooks and high-energy pacing."
        ),
        SampleVideo(
            title = "Veritasium: How One Line in the Law Created the Internet",
            channel = "Veritasium",
            videoId = "Y5e-r78r-yQ",
            description = "Mind-bending breakdown of section 230 and algorithm dynamics."
        )
    )

    data class SampleVideo(
        val title: String,
        val channel: String,
        val videoId: String,
        val description: String
    ) {
        val url: String get() = "https://www.youtube.com/watch?v=$videoId"
    }
}
