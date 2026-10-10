package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.YouTubeVideoInfo
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.AutoClipTertiary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ScoreGood
import kotlinx.coroutines.delay

@Composable
fun VideoProcessingProgressView(
    videoInfo: YouTubeVideoInfo?,
    stageTitle: String,
    stageDetail: String,
    currentStep: Int,
    totalSteps: Int,
    progress: Float,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth progress interpolation
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    // Pulse animation for active glowing badges
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Laser scan offset animation across video thumbnail
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laserScan"
    )

    // Cycling Creator Tips
    val creatorTips = remember {
        listOf(
            "The first 3 seconds decide whether a viewer stays or swipes. We are picking high-curiosity openings.",
            "Removing dead air and filler words increases retention by up to 35% on YouTube Shorts.",
            "Vertical 9:16 aspect ratio captures 100% of mobile screen real estate and doubles completion rate.",
            "Emotional soundbites and controversial statements generate 4x more comments and algorithmic reach."
        )
    }

    var tipIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            tipIndex = (tipIndex + 1) % creatorTips.size
        }
    }

    val stepTitles = remember {
        listOf(
            "Video & Stream Ingestion",
            "Audio & Content Energy Scanning",
            "Gemini 3.5 Virality & Retention AI",
            "Trimming Timestamps & Formatting Clips"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // AI Scanner Status Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AutoClipPrimary.copy(alpha = 0.15f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipSecondary))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(AutoClipPrimary.copy(alpha = pulseAlpha), CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PROCESSING YOUTUBE VIDEO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Video Thumbnail Preview Card with Animated Laser Scanner Line
        videoInfo?.let { video ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2E334D), Color(0xFF2E334D)))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("processing_video_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark gradient vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )

                        // Animated Laser Scan Line Overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val yPos = size.height * laserOffset
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        AutoClipPrimary.copy(alpha = 0.8f),
                                        AutoClipSecondary,
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(0f, yPos),
                                end = Offset(size.width, yPos),
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Duration / Live Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "YouTube Stream",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AutoClipTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Channel: ${video.authorName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFA6ACBF),
                        maxLines = 1
                    )
                }
            }
        } ?: run {
            // Skeleton while resolving video
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MovieCreation,
                            contentDescription = "Resolving",
                            tint = AutoClipPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connecting to YouTube...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Prominent Progress Bar Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFF2C3146), Color(0xFF202334)))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Percentage & Stage Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Generating Viral Clips",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Step $currentStep of $totalSteps",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoClipSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Large percentage badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF202334)
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AutoClipPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Glowing Gradient Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1F2232))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AutoClipPrimary,
                                        AutoClipSecondary,
                                        AutoClipTertiary
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Active Stage Text
                Text(
                    text = stageTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stageDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA2B8),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Spectrum Frequency Visualizer Simulation
                AudioFrequencyVisualizer(pulse = pulseAlpha)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pipeline Step Tracker Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151824)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFF262A3B), Color(0xFF262A3B)))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "AI Processing Pipeline",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCAD0E4)
                )

                Spacer(modifier = Modifier.height(12.dp))

                stepTitles.forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isDone = stepNum < currentStep
                    val isCurrent = stepNum == currentStep

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step status circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    when {
                                        isDone -> ScoreGood.copy(alpha = 0.2f)
                                        isCurrent -> AutoClipPrimary.copy(alpha = 0.25f)
                                        else -> Color(0xFF202334)
                                    },
                                    CircleShape
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        isDone -> ScoreGood
                                        isCurrent -> AutoClipPrimary
                                        else -> Color(0xFF383C52)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = ScoreGood,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "$stepNum",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else Color(0xFF7E849E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isDone -> Color(0xFFD4D8E6)
                                isCurrent -> Color.White
                                else -> Color(0xFF6B728E)
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AutoClipPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AutoClipPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Creator Virality Tip
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A172B)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(AutoClipSecondary.copy(0.4f), AutoClipSecondary.copy(0.4f)))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Creator Virality Insight",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = creatorTips[tipIndex],
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAD0E4),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cancel Processing Button
        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA0A6BC)),
            border = BorderStroke(1.dp, Color(0xFF383C52)),
            modifier = Modifier.testTag("cancel_generation_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cancel Processing")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun AudioFrequencyVisualizer(pulse: Float) {
    val barCount = 18
    val heights = remember {
        listOf(
            0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1.0f,
            0.7f, 0.4f, 0.85f, 0.6f, 0.95f, 0.5f,
            0.75f, 0.4f, 0.7f, 0.9f, 0.6f, 0.35f
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Color(0xFF141622), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Audio Stream",
                tint = AutoClipTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Audio Stream",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF888FA8)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            heights.forEachIndexed { idx, baseHeight ->
                val dynamicHeight = (baseHeight * ((pulse * 0.6f) + (idx % 3) * 0.2f)).coerceIn(0.15f, 1f)
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((20 * dynamicHeight).dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            if (idx % 2 == 0) AutoClipPrimary else AutoClipSecondary
                        )
                )
            }
        }
    }
}
