package com.example.data.model

import java.util.UUID

enum class ClipObjective(val label: String, val description: String) {
    VIRAL_SHORTS("Viral Shorts / Reels", "High retention hooks, fast pacing & punchy finish"),
    KEY_HIGHLIGHTS("Key Takeaways", "Crucial insights, core explanations & lessons"),
    FUNNY_MOMENTS("Funny / Reaction", "Humorous comments, reactions & unexpected moments"),
    QUOTES_WISDOM("Power Quotes", "Inspiring soundbites, mindset shifts & punchlines")
}

enum class ClipDurationTarget(val label: String, val rangeSeconds: IntRange) {
    SNAPPY_15_30("15–30s (Fast Hook)", 15..30),
    STANDARD_30_60("30–60s (Reels / Shorts)", 30..60),
    DEEP_60_90("60–90s (Full Concept)", 60..90)
}

data class YouTubeVideoInfo(
    val videoId: String,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
    val originalUrl: String,
    val durationApproxSeconds: Int = 300
) {
    val cleanWatchUrl: String
        get() = "https://www.youtube.com/watch?v=$videoId"

    val shortShareUrl: String
        get() = "https://youtu.be/$videoId"
}

data class ClipItem(
    val id: String = UUID.randomUUID().toString(),
    val videoId: String,
    val title: String,
    val startSeconds: Int,
    val endSeconds: Int,
    val viralScore: Int, // 1 - 100
    val hookScore: Int = (viralScore * 0.95).toInt().coerceIn(60, 99),
    val retentionScore: Int = (viralScore * 0.92).toInt().coerceIn(60, 99),
    val emotionalScore: Int = (viralScore * 0.9).toInt().coerceIn(60, 99),
    val hookReason: String,
    val transcriptSnippet: String,
    val hashtags: List<String> = listOf("#shorts", "#viral", "#clips"),
    val aspectRatio: String = "9:16 Shorts",
    val callToAction: String = "Follow for more highlights!",
    val isFavorite: Boolean = false
) {
    val durationSeconds: Int
        get() = (endSeconds - startSeconds).coerceAtLeast(1)

    val formattedTimeRange: String
        get() {
            val startMin = startSeconds / 60
            val startSec = startSeconds % 60
            val endMin = endSeconds / 60
            val endSec = endSeconds % 60
            return String.format("%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec)
        }

    val formattedDuration: String
        get() = "${durationSeconds}s"

    fun getYouTubeTimestampUrl(): String {
        return "https://youtu.be/$videoId?t=$startSeconds"
    }

    fun getEmbedUrl(autoplay: Boolean = true): String {
        val ap = if (autoplay) 1 else 0
        return "https://www.youtube-nocookie.com/embed/$videoId?start=$startSeconds&end=$endSeconds&autoplay=$ap&rel=0&modestbranding=1"
    }
}

data class VideoClipProject(
    val id: String = UUID.randomUUID().toString(),
    val videoInfo: YouTubeVideoInfo,
    val objective: ClipObjective,
    val clips: List<ClipItem>,
    val createdAt: Long = System.currentTimeMillis()
)
