package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.AutoClipTertiary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Virality Guide",
                    tint = AutoClipPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Viral Shorts & Reels Playbook",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tested formulas used by top short-form creators to maximize retention, watch time, and shares.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA0A6BC)
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.ElectricBolt,
                iconColor = AutoClipPrimary,
                title = "1. The 3-Second Hook Rule",
                description = "90% of swiping happens in the first 3 seconds. Never start with an intro ('Hey guys, welcome back'). Open in media res with high stakes, conflict, a contrarian opinion, or an intense visual cut."
            )
        }

        item {
            GuideCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = AutoClipSecondary,
                title = "2. Open Curiosity Loops",
                description = "Pose a question or introduce a mystery early on and delay the answer until the final 5 seconds. This forces the viewer's brain to stay engaged until the very end, driving 100%+ average retention."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.Lightbulb,
                iconColor = AutoClipTertiary,
                title = "3. Snappy Pacing & No Dead Air",
                description = "Long pauses cause scroll drop-off. Trim breathing pauses and filler words. Use AutoClip AI's fine-tune trim button to tighten the start and end by 1-2 seconds."
            )
        }

        item {
            GuideCard(
                icon = Icons.Default.Recommend,
                iconColor = Color(0xFFFFB800),
                title = "4. Clear Call to Action (CTA)",
                description = "Ask a specific binary or polarizing question in the comments (e.g. 'Would you do this? Yes or No?'). Comments trigger algorithmic promotion in YouTube Shorts and Reels feeds."
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B182B)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(AutoClipSecondary.copy(0.4f), AutoClipPrimary.copy(0.4f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pro Tip: Batch Publishing",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "From a single 30-minute podcast or video, AutoClip AI generates 5-6 viral clips. Post 2-3 clips per day across YouTube Shorts, TikTok, and Instagram Reels to capture 10x organic reach with zero extra filming.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAD0E4),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14241E)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00E676).copy(0.5f), Color(0xFF00B0FF).copy(0.5f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Recommend,
                                    contentDescription = "Install Guide",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "📲 How to Download APK to Phone",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. In Google AI Studio on your phone or PC, open the top-right header menu (⋮ or Export).\n2. Tap 'Download APK' or 'Generate APK'.\n3. The base APK will download to your phone's Downloads folder.\n4. Tap the APK file and select 'Install' to use AutoClip AI directly on your Android phone!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E5F5),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221727)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(AutoClipPrimary.copy(0.5f), AutoClipSecondary.copy(0.5f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AutoClipPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Video Guide",
                                    tint = AutoClipPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🎬 How to Save Video to Phone Gallery",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. On any generated clip, tap the Download icon.\n2. Tap 'Generate MP4 Video Clip (Short)'.\n3. The app renders a 9:16 vertical video with burned-in captions, hook, and viral score.\n4. It automatically saves into your phone's Gallery (Movies/AutoClipAI) & Downloads folder!\n5. Tap 'Play Video' or 'Share Video' to send directly to WhatsApp or TikTok.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E5F5),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GuideCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF262B3D), Color(0xFF262B3D)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B6CC),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
