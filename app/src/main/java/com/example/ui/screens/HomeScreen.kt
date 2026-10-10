package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.YouTubeHelper
import com.example.data.model.ClipDurationTarget
import com.example.data.model.ClipItem
import com.example.data.model.ClipObjective
import com.example.data.model.VideoClipProject
import com.example.data.model.YouTubeVideoInfo
import com.example.ui.components.ClipCard
import com.example.ui.components.ClipDownloadDialog
import com.example.ui.components.ClipEditorDialog
import com.example.ui.components.ClipPlayerView
import com.example.ui.components.VideoProcessingProgressView
import com.example.util.ClipDownloader
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.AutoClipTertiary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ScoreViralHigh
import com.example.ui.viewmodel.ClipGeneratorViewModel
import com.example.ui.viewmodel.GenerationUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: ClipGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val previewVideoInfo by viewModel.previewVideoInfo.collectAsStateWithLifecycle()
    val selectedObjective by viewModel.selectedObjective.collectAsStateWithLifecycle()
    val selectedDurationTarget by viewModel.selectedDurationTarget.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val activePreviewClip by viewModel.activePreviewClip.collectAsStateWithLifecycle()
    val editingClip by viewModel.editingClip.collectAsStateWithLifecycle()

    var filterCategory by remember { mutableStateOf("ALL") } // ALL, VIRAL_90, FAVORITES
    var downloadingClip by remember { mutableStateOf<ClipItem?>(null) }

    // Fine-tune dialog if open
    editingClip?.let { clip ->
        ClipEditorDialog(
            clip = clip,
            onDismiss = { viewModel.closeClipEditor() },
            onSave = { clipId, title, start, end ->
                viewModel.saveClipEdit(clipId, title, start, end)
            }
        )
    }

    // Download dialog if open
    downloadingClip?.let { clip ->
        (currentProject ?: (uiState as? GenerationUiState.Success)?.project)?.let { project ->
            ClipDownloadDialog(
                clip = clip,
                videoInfo = project.videoInfo,
                onDismiss = { downloadingClip = null }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (val state = uiState) {
            is GenerationUiState.Success -> {
                SuccessClipsView(
                    project = state.project,
                    activePreviewClip = activePreviewClip,
                    filterCategory = filterCategory,
                    onFilterChange = { filterCategory = it },
                    onPreviewClip = { viewModel.openClipPreview(it) },
                    onClosePreview = { viewModel.closeClipPreview() },
                    onFineTuneClip = { viewModel.openClipEditor(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                    onDownloadClip = { downloadingClip = it },
                    onNewVideoClick = { viewModel.resetToNewGeneration() }
                )
            }

            is GenerationUiState.GeneratingClips -> {
                VideoProcessingProgressView(
                    videoInfo = state.videoInfo,
                    stageTitle = state.stage,
                    stageDetail = state.detail,
                    currentStep = state.currentStep,
                    totalSteps = state.totalSteps,
                    progress = state.progress,
                    onCancel = { viewModel.cancelGeneration() }
                )
            }

            is GenerationUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.startClipGeneration() },
                    onDismiss = { viewModel.resetToNewGeneration() }
                )
            }

            GenerationUiState.Idle -> {
                InputGeneratorView(
                    urlInput = urlInput,
                    previewVideoInfo = previewVideoInfo,
                    selectedObjective = selectedObjective,
                    selectedDurationTarget = selectedDurationTarget,
                    onUrlChange = { viewModel.onUrlInputChanged(it) },
                    onPasteClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text?.toString().orEmpty()
                            if (text.isNotBlank()) {
                                viewModel.onUrlInputChanged(text)
                                Toast.makeText(context, "Pasted YouTube Link!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onClearClick = { viewModel.onUrlInputChanged("") },
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onObjectiveChange = { viewModel.setObjective(it) },
                    onDurationChange = { viewModel.setDurationTarget(it) },
                    onGenerateClick = { viewModel.startClipGeneration() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputGeneratorView(
    urlInput: String,
    previewVideoInfo: YouTubeVideoInfo?,
    selectedObjective: ClipObjective,
    selectedDurationTarget: ClipDurationTarget,
    onUrlChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onClearClick: () -> Unit,
    onSelectPreset: (YouTubeHelper.SampleVideo) -> Unit,
    onObjectiveChange: (ClipObjective) -> Unit,
    onDurationChange: (ClipDurationTarget) -> Unit,
    onGenerateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Title Banner
            HeroHeader()
        }

        // Paste URL Input Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF2E334A), Color(0xFF1E2130))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paste YouTube Video Link",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Supports standard YouTube links, youtu.be, shorts, and live streams",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA0A6BC),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Text Field with paste and clear icons
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUrlChange,
                        placeholder = {
                            Text("https://www.youtube.com/watch?v=...", color = Color(0xFF6B728E))
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = onClearClick,
                                        modifier = Modifier.testTag("clear_url_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onPasteClick,
                                    modifier = Modifier.testTag("paste_url_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = AutoClipPrimary
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AutoClipPrimary,
                            unfocusedBorderColor = Color(0xFF33384D),
                            focusedContainerColor = Color(0xFF0F1018),
                            unfocusedContainerColor = Color(0xFF0F1018)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_url_input")
                    )

                    // Live Detected Video Preview Card
                    AnimatedVisibility(visible = previewVideoInfo != null) {
                        previewVideoInfo?.let { video ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D202E)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(AutoClipPrimary.copy(0.4f), AutoClipSecondary.copy(0.4f)))
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(72.dp, 48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Channel: ${video.authorName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AutoClipTertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Test Presets Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Or Try Popular Presets (1-Tap)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Instant demo",
                        style = MaterialTheme.typography.labelSmall,
                        color = AutoClipSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(YouTubeHelper.SAMPLE_PRESETS) { sample ->
                        PresetCard(sample = sample, onClick = { onSelectPreset(sample) })
                    }
                }
            }
        }

        // Clip Objective Configuration
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2E334A), Color(0xFF1E2130)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Clip Style & Objective",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ClipObjective.entries.forEach { obj ->
                        val isSelected = obj == selectedObjective
                        Surface(
                            onClick = { onObjectiveChange(obj) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AutoClipSecondary.copy(alpha = 0.15f) else Color(0xFF191B27),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (isSelected) Brush.horizontalGradient(listOf(AutoClipSecondary, AutoClipSecondary))
                                else Brush.horizontalGradient(listOf(Color(0xFF282C3D), Color(0xFF282C3D)))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            if (isSelected) AutoClipSecondary else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(2.dp, if (isSelected) AutoClipSecondary else Color(0xFF555A72), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = obj.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFFD4D8E6)
                                    )
                                    Text(
                                        text = obj.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF9096AD)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration target selection
                    Text(
                        text = "Target Clip Length",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClipDurationTarget.entries.forEach { dur ->
                            val isSelected = dur == selectedDurationTarget
                            Surface(
                                onClick = { onDurationChange(dur) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AutoClipPrimary.copy(alpha = 0.2f) else Color(0xFF191B27),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = if (isSelected) Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipPrimary))
                                    else Brush.horizontalGradient(listOf(Color(0xFF2A2E40), Color(0xFF2A2E40)))
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dur.label.substringBefore(" ("),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AutoClipPrimary else Color.White
                                    )
                                    Text(
                                        text = dur.label.substringAfter("(").removeSuffix(")"),
                                        fontSize = 10.sp,
                                        color = Color(0xFF8F95AC)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary Action Button
        item {
            Button(
                onClick = onGenerateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AutoClipPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_clips_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Generate",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate AI Clips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroHeader() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF42091E),
                        Color(0xFF1E0E3B),
                        Color(0xFF101322)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(AutoClipPrimary.copy(0.4f), AutoClipSecondary.copy(0.4f))),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AutoClipPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieCreation,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI CLIP ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Powered by Gemini 3.5",
                    fontSize = 11.sp,
                    color = Color(0xFFC0A8F4)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Turn Long YouTube Videos Into Viral Clips",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Paste any link. AutoClip AI detects high-retention moments, cuts precise timestamps, crafts click-worthy hooks, and scores virality.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCAD0E4),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PresetCard(
    sample: YouTubeHelper.SampleVideo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF2C3042), Color(0xFF2C3042)))
        ),
        modifier = Modifier
            .width(200.dp)
            .testTag("preset_card_${sample.videoId}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = "https://img.youtube.com/vi/${sample.videoId}/hqdefault.jpg",
                contentDescription = sample.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sample.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sample.channel,
                style = MaterialTheme.typography.labelSmall,
                color = AutoClipPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ScoreViralHigh
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCAD0E4),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss) {
                Text("Change URL")
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AutoClipPrimary)
            ) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuccessClipsView(
    project: VideoClipProject,
    activePreviewClip: ClipItem?,
    filterCategory: String,
    onFilterChange: (String) -> Unit,
    onPreviewClip: (ClipItem) -> Unit,
    onClosePreview: () -> Unit,
    onFineTuneClip: (ClipItem) -> Unit,
    onToggleFavorite: (ClipItem) -> Unit,
    onDownloadClip: (ClipItem) -> Unit,
    onNewVideoClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val displayedClips = remember(project.clips, filterCategory) {
        when (filterCategory) {
            "VIRAL_90" -> project.clips.filter { it.viralScore >= 90 }
            "FAVORITES" -> project.clips.filter { it.isFavorite }
            else -> project.clips
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Video Header Summary Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2C3044), Color(0xFF2C3044)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = project.videoInfo.thumbnailUrl,
                        contentDescription = project.videoInfo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp, 54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.videoInfo.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${project.videoInfo.authorName} • ${project.clips.size} clips generated",
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoClipPrimary
                        )
                    }
                    IconButton(
                        onClick = onNewVideoClick,
                        modifier = Modifier.testTag("new_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Video",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Active Player View if a clip is open
        activePreviewClip?.let { activeClip ->
            item {
                ClipPlayerView(
                    clip = activeClip,
                    onClose = onClosePreview,
                    modifier = Modifier.testTag("active_clip_player")
                )
            }
        }

        // Filter chips bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = filterCategory == "ALL",
                        onClick = { onFilterChange("ALL") },
                        label = { Text("All (${project.clips.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AutoClipPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1B1D2A),
                            labelColor = Color(0xFFB0B6CC)
                        )
                    )
                    FilterChip(
                        selected = filterCategory == "VIRAL_90",
                        onClick = { onFilterChange("VIRAL_90") },
                        label = { Text("🔥 90+ Score") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScoreViralHigh,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1B1D2A),
                            labelColor = Color(0xFFB0B6CC)
                        )
                    )
                    FilterChip(
                        selected = filterCategory == "FAVORITES",
                        onClick = { onFilterChange("FAVORITES") },
                        label = { Text("★ Saved") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AutoClipSecondary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1B1D2A),
                            labelColor = Color(0xFFB0B6CC)
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download All Package
                    Surface(
                        onClick = {
                            scope.launch {
                                ClipDownloader.downloadAllClipsInProject(context, project)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = AutoClipPrimary.copy(alpha = 0.2f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(AutoClipPrimary, AutoClipPrimary))
                        ),
                        modifier = Modifier.testTag("download_all_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download All",
                                tint = AutoClipPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Download All",
                                fontSize = 11.sp,
                                color = AutoClipPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Copy All Summary
                    Surface(
                        onClick = {
                            val allText = buildString {
                                appendLine("🎬 Generated Clips for: ${project.videoInfo.title}")
                                appendLine("🔗 Video: ${project.videoInfo.originalUrl}")
                                appendLine()
                                project.clips.forEachIndexed { idx, c ->
                                    appendLine("#${idx + 1} ${c.title}")
                                    appendLine("⏱ Timestamp: ${c.formattedTimeRange}")
                                    appendLine("🔗 ${c.getYouTubeTimestampUrl()}")
                                    appendLine("🎯 Hook: ${c.hookReason}")
                                    appendLine("\"${c.transcriptSnippet}\"")
                                    appendLine(c.hashtags.joinToString(" "))
                                    appendLine()
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("All Clips", allText))
                            Toast.makeText(context, "Copied all clips package!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF222638)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy all",
                                tint = Color.White.copy(0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Clips List
        itemsIndexed(displayedClips) { index, clip ->
            ClipCard(
                clip = clip,
                index = index,
                isSelectedForPreview = activePreviewClip?.id == clip.id,
                onPreviewClick = { onPreviewClip(clip) },
                onFineTuneClick = { onFineTuneClip(clip) },
                onToggleFavorite = { onToggleFavorite(clip) },
                onDownloadClick = { onDownloadClip(clip) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
