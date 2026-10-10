package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipItem
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.ScoreGood
import com.example.ui.theme.ScoreHigh
import com.example.ui.theme.ScoreViralHigh

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClipCard(
    clip: ClipItem,
    index: Int,
    isSelectedForPreview: Boolean,
    onPreviewClick: () -> Unit,
    onFineTuneClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val viralColor = when {
        clip.viralScore >= 90 -> ScoreViralHigh
        clip.viralScore >= 80 -> ScoreHigh
        else -> ScoreGood
    }

    val cardBorderBrush = if (isSelectedForPreview) {
        Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipSecondary))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF2E3246), Color(0xFF222533)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clip_card_$index")
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelectedForPreview) 2.dp else 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForPreview) Color(0xFF181A27) else Color(0xFF141620)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Rank + Time Range + Viral Score Badge + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Clip index badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF242838),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Time range pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E2130)
                    ) {
                        Text(
                            text = "⏱ ${clip.formattedTimeRange} (${clip.formattedDuration})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFCAD0E0),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Viral Score Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = viralColor.copy(alpha = 0.18f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(viralColor, viralColor))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Viral Score",
                                tint = viralColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${clip.viralScore}/100",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = viralColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clip_fav_button_$index")
                    ) {
                        Icon(
                            imageVector = if (clip.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite clip",
                            tint = if (clip.isFavorite) AutoClipPrimary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clip Title / Hook
            Text(
                text = clip.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Why it hooks badge / explanation
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1B1E2B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "🎯 Hook:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AutoClipSecondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = clip.hookReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB4B9C9),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quotable Transcript Snippet
            if (clip.transcriptSnippet.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10121C),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF26293A), Color(0xFF26293A)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Soundbite",
                            tint = AutoClipPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = clip.transcriptSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E4EC),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Hashtags FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                clip.hashtags.take(5).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF202334)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            color = Color(0xFF9EA4BA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preview Clip Button
                FilledTonalButton(
                    onClick = onPreviewClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_clip_button_$index"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelectedForPreview) AutoClipPrimary else Color(0xFF25293A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelectedForPreview) "Playing" else "Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Fine-tune Timestamps Cut
                OutlinedButton(
                    onClick = onFineTuneClick,
                    modifier = Modifier.testTag("finetune_clip_button_$index"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCAD0E0)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF383C52)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Fine Tune Cut",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Trim", fontSize = 13.sp)
                }

                // Download Clip Option
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(AutoClipPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, AutoClipPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .testTag("download_clip_button_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Clip Package",
                        tint = AutoClipPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy Clip Package & Share
                IconButton(
                    onClick = {
                        val shareText = """
                            🎬 ${clip.title}
                            ⏱ Timestamp: ${clip.formattedTimeRange}
                            🔗 Watch: ${clip.getYouTubeTimestampUrl()}
                            
                            "${clip.transcriptSnippet}"
                            
                            ${clip.hashtags.joinToString(" ")}
                        """.trimIndent()

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AutoClip AI Clip", shareText))
                        Toast.makeText(context, "Clip details & timestamp copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF202334), RoundedCornerShape(10.dp))
                        .testTag("copy_clip_button_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Clip Info",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share Sheet
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${clip.title}\n${clip.getYouTubeTimestampUrl()}\n\n${clip.hashtags.joinToString(" ")}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Clip"))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF202334), RoundedCornerShape(10.dp))
                        .testTag("share_clip_button_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
