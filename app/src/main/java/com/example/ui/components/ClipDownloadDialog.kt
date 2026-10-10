package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipItem
import com.example.data.model.YouTubeVideoInfo
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.AutoClipTertiary
import com.example.util.ClipDownloader
import com.example.util.ClipVideoExporter
import kotlinx.coroutines.launch

@Composable
fun ClipDownloadDialog(
    clip: ClipItem,
    videoInfo: YouTubeVideoInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRenderingVideo by remember { mutableStateOf(false) }
    var renderProgress by remember { mutableFloatStateOf(0f) }
    var renderStatusText by remember { mutableStateOf("Initializing video creator...") }
    var completedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBundleDownloading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isRenderingVideo) onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AutoClipPrimary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Download Clip & Video",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (!isRenderingVideo) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Clip info summary header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B1E2D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = clip.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⏱ ${clip.formattedTimeRange} (${clip.formattedDuration}) • Viral Score ${clip.viralScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoClipTertiary
                        )
                    }
                }

                // If currently rendering video
                if (isRenderingVideo) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161928)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipSecondary))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = AutoClipPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Rendering MP4 Video Clip...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { renderProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = AutoClipPrimary,
                                trackColor = Color(0xFF25293E)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = renderStatusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB4BAD0),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${(renderProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AutoClipPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If video render finished successfully
                completedVideoUri?.let { uri ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13231D)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MP4 Video Ready & Saved!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saved to Gallery & Downloads/AutoClipAI. Ready for TikTok, Reels & Shorts!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAD0E4),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { ClipVideoExporter.playVideo(context, uri) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Video", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { ClipVideoExporter.shareVideo(context, uri, clip.title) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AutoClipPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Video", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (!isRenderingVideo && completedVideoUri == null) {
                    Text(
                        text = "Choose Download Format:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCAD0E4)
                    )

                    // Option 1: Direct Video (MP4) Render on Device
                    DownloadOptionItem(
                        icon = Icons.Default.Movie,
                        iconColor = AutoClipPrimary,
                        title = "Generate MP4 Video Clip (Short)",
                        subtitle = "Renders 9:16 HD video with burned-in subtitles, hook & timer directly to your phone!",
                        badge = "DIRECT MP4",
                        onClick = {
                            isRenderingVideo = true
                            errorMessage = null
                            scope.launch {
                                val result = ClipDownloader.exportRealMp4Video(
                                    context = context,
                                    clip = clip,
                                    videoInfo = videoInfo,
                                    onProgress = { p, status ->
                                        renderProgress = p
                                        renderStatusText = status
                                    }
                                )
                                isRenderingVideo = false
                                if (result.success && result.videoUri != null) {
                                    completedVideoUri = result.videoUri
                                } else {
                                    errorMessage = result.errorMessage ?: "Failed to export video"
                                }
                            }
                        }
                    )

                    // Option 2: Full Original Video Downloader
                    DownloadOptionItem(
                        icon = Icons.Default.OpenInBrowser,
                        iconColor = AutoClipSecondary,
                        title = "Download Full Original Video",
                        subtitle = "Fast direct download link to save full video MP4 (720p/1080p)",
                        badge = "FULL VIDEO",
                        onClick = {
                            ClipDownloader.openFullVideoDownloadPortal(context, clip)
                            onDismiss()
                        }
                    )

                    // Option 3: Complete Bundle
                    DownloadOptionItem(
                        icon = Icons.Default.FolderZip,
                        iconColor = Color(0xFF00E5FF),
                        title = "Complete Creator Bundle",
                        subtitle = "Saves MP4 Video, Subtitles (.srt), Script (.txt) & Poster",
                        badge = "ALL-IN-ONE",
                        onClick = {
                            isBundleDownloading = true
                            scope.launch {
                                ClipDownloader.downloadClipPackage(context, clip, videoInfo)
                                isBundleDownloading = false
                                onDismiss()
                            }
                        }
                    )

                    // Option 4: Subtitles file only (.srt)
                    DownloadOptionItem(
                        icon = Icons.Default.Subtitles,
                        iconColor = AutoClipTertiary,
                        title = "Subtitles File (.SRT)",
                        subtitle = "Exact timestamps ready for CapCut, Premiere & DaVinci",
                        badge = "SUBTITLES",
                        onClick = {
                            scope.launch {
                                ClipDownloader.downloadSubtitlesOnly(context, clip)
                                onDismiss()
                            }
                        }
                    )

                    // Option 5: Poster / Thumbnail
                    DownloadOptionItem(
                        icon = Icons.Default.Image,
                        iconColor = Color(0xFFFFB800),
                        title = "High-Res Poster Frame (.JPG)",
                        subtitle = "Saves cover image to Downloads/AutoClipAI",
                        onClick = {
                            ClipDownloader.downloadVideoStream(context, clip)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0A6BC))
            ) {
                Text(if (completedVideoUri != null) "Done" else "Close")
            }
        },
        containerColor = Color(0xFF141724),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DownloadOptionItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191C2B)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF2C3146), Color(0xFF2C3146)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    badge?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = iconColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = it,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = iconColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA2B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Save",
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
