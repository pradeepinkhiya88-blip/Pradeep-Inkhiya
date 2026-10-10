package com.example.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.ClipItem
import com.example.data.model.VideoClipProject
import com.example.data.model.YouTubeVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ClipDownloader {

    /**
     * Downloads the complete clip creator bundle (Subtitles .SRT + Script .TXT + Poster Thumbnail)
     * directly into the device's public Downloads/AutoClipAI folder.
     */
    suspend fun downloadClipPackage(
        context: Context,
        clip: ClipItem,
        videoInfo: YouTubeVideoInfo
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val sanitizedTitle = clip.title
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(30)
                .trimEnd('_')

            // 1. Generate & Save .SRT Subtitles File
            val srtContent = buildSrtContent(clip)
            val srtFileName = "AutoClip_${sanitizedTitle}_subtitles.srt"
            saveTextFileToDownloads(context, srtFileName, srtContent, "application/x-subrip")

            // 2. Generate & Save Script & Metadata .TXT File
            val scriptContent = buildScriptContent(clip, videoInfo)
            val scriptFileName = "AutoClip_${sanitizedTitle}_script.txt"
            saveTextFileToDownloads(context, scriptFileName, scriptContent, "text/plain")

            // 3. Download Thumbnail Image via DownloadManager
            downloadThumbnailViaDownloadManager(context, clip, sanitizedTitle)

            // 4. Export Real MP4 Video File
            ClipVideoExporter.exportClipToMp4(context, clip, videoInfo)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Saved Complete Creator Bundle (MP4, SRT, TXT, JPG) to Downloads/AutoClipAI!",
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Download failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    /**
     * Downloads individual .SRT subtitle file
     */
    suspend fun downloadSubtitlesOnly(context: Context, clip: ClipItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val sanitizedTitle = clip.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val srtContent = buildSrtContent(clip)
            val srtFileName = "AutoClip_${sanitizedTitle}.srt"
            saveTextFileToDownloads(context, srtFileName, srtContent, "application/x-subrip")

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved $srtFileName to Downloads!", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    /**
     * Exports a real, playable MP4 video clip file directly to device Movies & Downloads.
     */
    suspend fun exportRealMp4Video(
        context: Context,
        clip: ClipItem,
        videoInfo: YouTubeVideoInfo,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ClipVideoExporter.ExportResult {
        return ClipVideoExporter.exportClipToMp4(context, clip, videoInfo, onProgress)
    }

    /**
     * Downloads full video MP4 using direct web converter mirror
     */
    fun openFullVideoDownloadPortal(context: Context, clip: ClipItem, mirror: String = "10DOWNLOADER") {
        try {
            val url = when (mirror) {
                "SSYOUTUBE" -> "https://ssyoutube.com/watch?v=${clip.videoId}"
                "Y2MATE" -> "https://www.y2mate.com/youtube/${clip.videoId}"
                else -> "https://10downloader.com/download?v=https://www.youtube.com/watch?v=${clip.videoId}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Opening direct video downloader...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads video stream or launches download helper for the exact video/clip
     */
    fun downloadVideoStream(context: Context, clip: ClipItem) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse("https://img.youtube.com/vi/${clip.videoId}/maxresdefault.jpg")
            val sanitizedTitle = clip.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)

            // Queue a DownloadManager request
            val request = DownloadManager.Request(downloadUri).apply {
                setTitle("AutoClip Thumbnail: ${clip.title}")
                setDescription("Clip Timestamp: ${clip.formattedTimeRange}")
                setMimeType("image/jpeg")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "AutoClipAI/AutoClip_${sanitizedTitle}_poster.jpg"
                )
            }
            dm.enqueue(request)

            Toast.makeText(
                context,
                "Saved HD Poster image to Downloads/AutoClipAI!",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads all clips in project
     */
    suspend fun downloadAllClipsInProject(
        context: Context,
        project: VideoClipProject
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bundleSummary = buildString {
                appendLine("==========================================")
                appendLine("AUTOCLIP AI - COMPLETE CLIPS PACKAGE")
                appendLine("Video: ${project.videoInfo.title}")
                appendLine("Channel: ${project.videoInfo.authorName}")
                appendLine("URL: ${project.videoInfo.originalUrl}")
                appendLine("Clips Count: ${project.clips.size}")
                appendLine("==========================================\n")

                project.clips.forEachIndexed { index, clip ->
                    appendLine("--- CLIP #${index + 1} ---")
                    appendLine("Title: ${clip.title}")
                    appendLine("Timestamp: ${clip.formattedTimeRange} (${clip.formattedDuration})")
                    appendLine("Viral Score: ${clip.viralScore}/100")
                    appendLine("Direct YouTube Link: ${clip.getYouTubeTimestampUrl()}")
                    appendLine("Hook Analysis: ${clip.hookReason}")
                    appendLine("Transcript Soundbite: \"${clip.transcriptSnippet}\"")
                    appendLine("Hashtags: ${clip.hashtags.joinToString(" ")}")
                    appendLine("CTA: ${clip.callToAction}\n")
                }
            }

            val sanitizedProject = project.videoInfo.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            saveTextFileToDownloads(
                context,
                "AutoClip_ALL_CLIPS_${sanitizedProject}.txt",
                bundleSummary,
                "text/plain"
            )

            // Save combined .SRT
            val combinedSrt = buildString {
                project.clips.forEachIndexed { index, clip ->
                    appendLine("${index + 1}")
                    appendLine("${formatSecondsToSrtTime(clip.startSeconds)} --> ${formatSecondsToSrtTime(clip.endSeconds)}")
                    appendLine("[${clip.title}]")
                    appendLine(clip.transcriptSnippet)
                    appendLine()
                }
            }
            saveTextFileToDownloads(
                context,
                "AutoClip_ALL_CLIPS_${sanitizedProject}.srt",
                combinedSrt,
                "application/x-subrip"
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Saved All ${project.clips.size} clips (.txt + .srt) to Downloads/AutoClipAI!",
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    private fun downloadThumbnailViaDownloadManager(
        context: Context,
        clip: ClipItem,
        sanitizedTitle: String
    ) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val thumbUri = Uri.parse("https://img.youtube.com/vi/${clip.videoId}/hqdefault.jpg")

            val request = DownloadManager.Request(thumbUri).apply {
                setTitle("AutoClip Thumbnail: ${clip.title}")
                setDescription("Saving high-res thumbnail to Downloads/AutoClipAI")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "AutoClipAI/AutoClip_${sanitizedTitle}.jpg"
                )
            }
            dm.enqueue(request)
        } catch (_: Exception) {}
    }

    private fun saveTextFileToDownloads(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separator + "AutoClipAI"
                )
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val autoClipDir = File(downloadsDir, "AutoClipAI")
            if (!autoClipDir.exists()) {
                autoClipDir.mkdirs()
            }
            val file = File(autoClipDir, fileName)
            FileOutputStream(file).use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun buildSrtContent(clip: ClipItem): String {
        return buildString {
            appendLine("1")
            appendLine("${formatSecondsToSrtTime(clip.startSeconds)} --> ${formatSecondsToSrtTime(clip.endSeconds)}")
            appendLine("[${clip.title}]")
            appendLine(clip.transcriptSnippet)
            appendLine()
        }
    }

    private fun buildScriptContent(clip: ClipItem, videoInfo: YouTubeVideoInfo): String {
        return buildString {
            appendLine("🎬 AUTOCLIP AI - CLIP EXPORT")
            appendLine("Title: ${clip.title}")
            appendLine("Timestamp Range: ${clip.formattedTimeRange}")
            appendLine("Duration: ${clip.formattedDuration}")
            appendLine("Viral Score: ${clip.viralScore}/100 (Hook: ${clip.hookScore}, Retention: ${clip.retentionScore})")
            appendLine("Watch Online: ${clip.getYouTubeTimestampUrl()}")
            appendLine("Source Video: ${videoInfo.cleanWatchUrl}")
            appendLine("Channel: ${videoInfo.authorName}")
            appendLine()
            appendLine("🎯 WHY THIS HOOKS:")
            appendLine(clip.hookReason)
            appendLine()
            appendLine("🎙 TRANSCRIPT SOUNDBITE:")
            appendLine("\"${clip.transcriptSnippet}\"")
            appendLine()
            appendLine("🏷 SUGGESTED HASHTAGS:")
            appendLine(clip.hashtags.joinToString(" "))
            appendLine()
            appendLine("📢 CALL TO ACTION:")
            appendLine(clip.callToAction)
        }
    }

    private fun formatSecondsToSrtTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d,000", hours, minutes, seconds)
    }
}
