package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch

@Composable
fun ClipDownloadDialog(
    clip: ClipItem,
    videoInfo: YouTubeVideoInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        text = "Download Clip",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(0.7f),
                        modifier = Modifier.size(18.dp)
                    )
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
                // Clip info summary
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Choose Download Format:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCAD0E4)
                )

                // Option 1: Direct Video (MP4)
                DownloadOptionItem(
                    icon = Icons.Default.Movie,
                    iconColor = AutoClipPrimary,
                    title = "Download Video (MP4 File)",
                    subtitle = "Saves HD MP4 video clip to Downloads/AutoClipAI",
                    badge = "MP4 VIDEO",
                    onClick = {
                        ClipDownloader.downloadVideoMp4File(context, clip)
                        onDismiss()
                    }
                )

                // Option 2: Complete Bundle
                DownloadOptionItem(
                    icon = Icons.Default.FolderZip,
                    iconColor = AutoClipSecondary,
                    title = "Complete Creator Bundle",
                    subtitle = "Saves MP4 Video, Subtitles (.srt), and Script (.txt)",
                    badge = "ALL-IN-ONE",
                    onClick = {
                        isDownloading = true
                        scope.launch {
                            ClipDownloader.downloadClipPackage(context, clip, videoInfo)
                            isDownloading = false
                            onDismiss()
                        }
                    }
                )

                // Option 2: Subtitles file only (.srt)
                DownloadOptionItem(
                    icon = Icons.Default.Subtitles,
                    iconColor = AutoClipSecondary,
                    title = "Subtitles File (.SRT)",
                    subtitle = "Exact timestamps ready for CapCut, Premiere & DaVinci",
                    badge = "EDITOR READY",
                    onClick = {
                        isDownloading = true
                        scope.launch {
                            ClipDownloader.downloadSubtitlesOnly(context, clip)
                            isDownloading = false
                            onDismiss()
                        }
                    }
                )

                // Option 3: Poster / Thumbnail
                DownloadOptionItem(
                    icon = Icons.Default.Image,
                    iconColor = Color(0xFFFFB800),
                    title = "High-Res Poster Image (.JPG)",
                    subtitle = "Saves thumbnail frame to Downloads/AutoClipAI",
                    onClick = {
                        ClipDownloader.downloadVideoStream(context, clip)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0A6BC))
            ) {
                Text("Close")
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
