package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.YouTubeHelper
import com.example.data.local.AppDatabase
import com.example.data.model.ClipDurationTarget
import com.example.data.model.ClipItem
import com.example.data.model.ClipObjective
import com.example.data.model.VideoClipProject
import com.example.data.model.YouTubeVideoInfo
import com.example.data.repository.ClipRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class GeneratingClips(
        val videoInfo: YouTubeVideoInfo?,
        val stage: String,
        val detail: String,
        val currentStep: Int, // 1 to 4
        val totalSteps: Int = 4,
        val progress: Float // 0.0 to 1.0
    ) : GenerationUiState
    data class Success(val project: VideoClipProject) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

enum class NavTab {
    GENERATE,
    PROJECTS,
    GUIDE
}

class ClipGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClipRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = ClipRepository(db.clipDao())
    }

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _previewVideoInfo = MutableStateFlow<YouTubeVideoInfo?>(null)
    val previewVideoInfo: StateFlow<YouTubeVideoInfo?> = _previewVideoInfo.asStateFlow()

    private val _selectedObjective = MutableStateFlow(ClipObjective.VIRAL_SHORTS)
    val selectedObjective: StateFlow<ClipObjective> = _selectedObjective.asStateFlow()

    private val _selectedDurationTarget = MutableStateFlow(ClipDurationTarget.STANDARD_30_60)
    val selectedDurationTarget: StateFlow<ClipDurationTarget> = _selectedDurationTarget.asStateFlow()

    private val _uiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _currentProject = MutableStateFlow<VideoClipProject?>(null)
    val currentProject: StateFlow<VideoClipProject?> = _currentProject.asStateFlow()

    private val _activePreviewClip = MutableStateFlow<ClipItem?>(null)
    val activePreviewClip: StateFlow<ClipItem?> = _activePreviewClip.asStateFlow()

    private val _activeTab = MutableStateFlow(NavTab.GENERATE)
    val activeTab: StateFlow<NavTab> = _activeTab.asStateFlow()

    private val _savedProjects = MutableStateFlow<List<VideoClipProject>>(emptyList())
    val savedProjects: StateFlow<List<VideoClipProject>> = _savedProjects.asStateFlow()

    private val _editingClip = MutableStateFlow<ClipItem?>(null)
    val editingClip: StateFlow<ClipItem?> = _editingClip.asStateFlow()

    private var resolveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.savedProjectsFlow.collectLatest { list ->
                _savedProjects.value = list
            }
        }
    }

    fun onUrlInputChanged(newUrl: String) {
        _urlInput.value = newUrl
        // Debounce auto-detecting YouTube video ID
        resolveJob?.cancel()
        val videoId = YouTubeHelper.extractVideoId(newUrl)
        if (videoId != null) {
            resolveJob = viewModelScope.launch {
                delay(400) // slight debounce
                try {
                    val info = repository.resolveVideo(newUrl)
                    _previewVideoInfo.value = info
                } catch (_: Exception) {
                    // ignore typing intermediate
                }
            }
        } else {
            _previewVideoInfo.value = null
        }
    }

    fun selectPreset(preset: YouTubeHelper.SampleVideo) {
        _urlInput.value = preset.url
        _previewVideoInfo.value = YouTubeVideoInfo(
            videoId = preset.videoId,
            title = preset.title,
            authorName = preset.channel,
            thumbnailUrl = "https://img.youtube.com/vi/${preset.videoId}/hqdefault.jpg",
            originalUrl = preset.url
        )
    }

    fun setObjective(objective: ClipObjective) {
        _selectedObjective.value = objective
    }

    fun setDurationTarget(target: ClipDurationTarget) {
        _selectedDurationTarget.value = target
    }

    fun setTab(tab: NavTab) {
        _activeTab.value = tab
    }

    private var generationJob: Job? = null

    fun startClipGeneration() {
        val input = _urlInput.value.trim()
        val videoId = YouTubeHelper.extractVideoId(input)
        if (videoId == null) {
            _uiState.value = GenerationUiState.Error("Please enter a valid YouTube video URL or ID (e.g. youtu.be/... or youtube.com/watch?v=...)")
            return
        }

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            try {
                // Step 1: Resolving Video Metadata
                _uiState.value = GenerationUiState.GeneratingClips(
                    videoInfo = _previewVideoInfo.value,
                    stage = "Step 1 of 4: Fetching Video Stream & Metadata",
                    detail = "Connecting to YouTube stream and extracting high-res assets...",
                    currentStep = 1,
                    totalSteps = 4,
                    progress = 0.15f
                )

                val videoInfo = _previewVideoInfo.value ?: repository.resolveVideo(input)
                _previewVideoInfo.value = videoInfo

                // Step 2: Audio & Transcript Analysis
                _uiState.value = GenerationUiState.GeneratingClips(
                    videoInfo = videoInfo,
                    stage = "Step 2 of 4: Scanning Audio Energy & Transcript",
                    detail = "Detecting speech cadence, soundbite peaks, and emotional topic shifts...",
                    currentStep = 2,
                    totalSteps = 4,
                    progress = 0.40f
                )
                delay(700)

                // Step 3: Gemini 3.5 AI Analysis
                _uiState.value = GenerationUiState.GeneratingClips(
                    videoInfo = videoInfo,
                    stage = "Step 3 of 4: Evaluating Viral Retention with Gemini 3.5",
                    detail = "AI is analyzing first-3-second hooks and audience watch-time models...",
                    currentStep = 3,
                    totalSteps = 4,
                    progress = 0.70f
                )

                val project = repository.generateClipsForVideo(
                    videoInfo = videoInfo,
                    objective = _selectedObjective.value,
                    durationTarget = _selectedDurationTarget.value
                )

                // Step 4: Finalizing Timestamps & Formatting
                _uiState.value = GenerationUiState.GeneratingClips(
                    videoInfo = videoInfo,
                    stage = "Step 4 of 4: Trimming Timestamps & Formatting Shorts",
                    detail = "Finalizing ${project.clips.size} clips, viral hook titles, and hashtags...",
                    currentStep = 4,
                    totalSteps = 4,
                    progress = 0.95f
                )
                delay(500)

                _currentProject.value = project
                _uiState.value = GenerationUiState.Success(project)
                _activePreviewClip.value = null // User can tap Preview on any clip
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _uiState.value = GenerationUiState.Idle
                } else {
                    _uiState.value = GenerationUiState.Error("Failed to generate clips: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        _uiState.value = GenerationUiState.Idle
    }

    fun selectProjectFromHistory(project: VideoClipProject) {
        _currentProject.value = project
        _activePreviewClip.value = project.clips.firstOrNull()
        _uiState.value = GenerationUiState.Success(project)
        _activeTab.value = NavTab.GENERATE
    }

    fun deleteProject(project: VideoClipProject) {
        viewModelScope.launch {
            repository.deleteProject(project.id)
            if (_currentProject.value?.id == project.id) {
                _currentProject.value = null
                _uiState.value = GenerationUiState.Idle
                _activePreviewClip.value = null
            }
        }
    }

    fun openClipPreview(clip: ClipItem) {
        _activePreviewClip.value = clip
    }

    fun closeClipPreview() {
        _activePreviewClip.value = null
    }

    fun openClipEditor(clip: ClipItem) {
        _editingClip.value = clip
    }

    fun closeClipEditor() {
        _editingClip.value = null
    }

    fun saveClipEdit(clipId: String, newTitle: String, newStart: Int, newEnd: Int) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val updated = repository.updateClipTimestamps(project, clipId, newStart, newEnd, newTitle)
            _currentProject.value = updated
            if (_activePreviewClip.value?.id == clipId) {
                _activePreviewClip.value = updated.clips.firstOrNull { it.id == clipId }
            }
            _editingClip.value = null
        }
    }

    fun toggleFavorite(clipId: String) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val updated = repository.toggleFavorite(project, clipId)
            _currentProject.value = updated
            if (_activePreviewClip.value?.id == clipId) {
                _activePreviewClip.value = updated.clips.firstOrNull { it.id == clipId }
            }
        }
    }

    fun resetToNewGeneration() {
        _uiState.value = GenerationUiState.Idle
        _currentProject.value = null
        _activePreviewClip.value = null
        _urlInput.value = ""
        _previewVideoInfo.value = null
    }
}
