package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ClipItem
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ClipPlayerView(
    clip: ClipItem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isShortsFraming by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var playerKey by remember(clip.id, clip.startSeconds, clip.endSeconds) { mutableStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF13151F)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(AutoClipPrimary.copy(alpha = 0.5f), AutoClipSecondary.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Player Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AutoClipPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clip Preview: ${clip.formattedTimeRange}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AutoClipSecondary.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = clip.formattedDuration,
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoClipSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle 9:16 Shorts Framing
                    IconButton(
                        onClick = { isShortsFraming = !isShortsFraming },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isShortsFraming) Icons.Default.StayCurrentLandscape else Icons.Default.CropPortrait,
                            contentDescription = "Toggle Framing",
                            tint = if (isShortsFraming) AutoClipSecondary else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Reload Clip from Start
                    IconButton(
                        onClick = {
                            playerKey++
                            webViewRef?.reload()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay Clip",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Close Preview
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player Viewport Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (isShortsFraming) {
                            it
                                .height(380.dp)
                                .clip(RoundedCornerShape(14.dp))
                        } else {
                            it
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(14.dp))
                        }
                    }
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // If in shorts framing mode, center a 9:16 aspect container
                Box(
                    modifier = Modifier
                        .let {
                            if (isShortsFraming) {
                                it
                                    .aspectRatio(9f / 16f)
                                    .fillMaxSize()
                                    .border(1.dp, AutoClipSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                            } else {
                                it.fillMaxSize()
                            }
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            try {
                                WebView(ctx).apply {
                                    // Use software layer to prevent MESA rendernode DRI device errors
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                    setBackgroundColor(android.graphics.Color.BLACK)
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedError(
                                            view: WebView?,
                                            errorCode: Int,
                                            description: String?,
                                            failingUrl: String?
                                        ) {}
                                    }
                                    webViewRef = this
                                    loadUrl(clip.getEmbedUrl(autoplay = true))
                                }
                            } catch (_: Exception) {
                                WebView(ctx)
                            }
                        },
                        update = { webView ->
                            webViewRef = webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // If in shorts framing, show overlay indicator
                if (isShortsFraming) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "9:16 Shorts Mode",
                            fontSize = 10.sp,
                            color = AutoClipSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Open in YouTube
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clip.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Auto-stops at ${clip.endSeconds / 60}:${String.format("%02d", clip.endSeconds % 60)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clip.getYouTubeTimestampUrl()))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = AutoClipPrimary.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipPrimary)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in YouTube",
                            tint = AutoClipPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Open on YouTube",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AutoClipPrimary
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(clip.id) {
        onDispose {
            try {
                webViewRef?.stopLoading()
                webViewRef?.loadUrl("about:blank")
                webViewRef?.destroy()
            } catch (_: Exception) {}
            webViewRef = null
        }
    }
}
