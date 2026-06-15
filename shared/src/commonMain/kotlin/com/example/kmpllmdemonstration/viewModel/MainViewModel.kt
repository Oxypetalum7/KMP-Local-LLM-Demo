package com.example.kmpllmdemonstration.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.example.kmpllmdemonstration.GenTextState
import com.example.kmpllmdemonstration.LlamaBridgeService
import com.example.kmpllmdemonstration.model.DownloadProgress
import com.example.kmpllmdemonstration.model.LlamaModel
import com.example.kmpllmdemonstration.model.ModelDownloader
import com.example.kmpllmdemonstration.model.ModelFileProvider
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUuidApi::class)
class MainViewModel(
    private val llamaBridgeService: LlamaBridgeService,
    private val modelFileProvider: ModelFileProvider,
    private val modelDownloader: ModelDownloader,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    private var generateJob: Option<Job> = none()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val model = LlamaModel.all.firstOrNull() ?: return@launch
            if (modelFileProvider.exists(model.fileName)) {
                initModel(model, modelFileProvider.getFilePath(model.fileName))
            }
        }
    }

    fun dispatch(intent: Intent) {
        when (intent) {
            is Intent.Main.UpdateInput -> _state.update { it.copy(input = intent.text) }
            Intent.Main.Send -> onSend()
            Intent.Main.LogoTapped -> onLogoTapped()
            is Intent.Main.TogglePromptCollapse -> onTogglePromptCollapse(intent.turnId)
            Intent.Main.StartNewChat -> onStartNewChat()
            Intent.Main.ToggleSidebar -> _state.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
            Intent.Main.DismissError -> onDismissError()
            is Intent.Setting.ToggleGpu -> onToggleGpu(intent.enabled)
        }
    }

    private fun onLogoTapped() {
        if (!_state.value.isLogoTappable) return
        downloadAndInit(LlamaModel.Gemma4E4BQ2)
    }

    private fun onSend() {
        val current = _state.value
        if (!current.isSendVisible) return
        val prompt = current.input.trim()
        val turnId = generateTurnId()
        _state.update {
            it.copy(
                input = "",
                turns = it.turns + ChatTurn(
                    id = turnId,
                    prompt = prompt,
                    response = ResponseState.Generating(""),
                ),
            )
        }
        generate(prompt, turnId)
    }

    private fun onTogglePromptCollapse(turnId: String) {
        _state.update { state ->
            state.copy(
                turns = state.turns.map { t ->
                    if (t.id == turnId) t.copy(isPromptCollapsed = !t.isPromptCollapsed) else t
                },
            )
        }
    }

    private fun onStartNewChat() {
        viewModelScope.launch {
            generateJob.onSome { job ->
                llamaBridgeService.cancelGenerate()
                job.join()
            }
            _state.update { it.copy(input = "", turns = emptyList(), isSidebarOpen = false) }
        }
    }

    private fun onToggleGpu(enabled: Boolean) {
        _state.update { it.copy(gpuEnabled = enabled) }
        viewModelScope.launch(Dispatchers.Default) {
            llamaBridgeService.updateParams(gpuEnabled = enabled)
        }
    }

    private fun onDismissError() {
        _state.update { state ->
            val newModel =
                if (state.model is ModelStatus.Failed) ModelStatus.NotInitialized else state.model
            state.copy(model = newModel)
        }
    }

    private fun downloadAndInit(model: LlamaModel) {
        viewModelScope.launch {
            val path = modelFileProvider.getFilePath(model.fileName)
            modelDownloader.download(model.downloadUrl, path).collect { progress ->
                when (progress) {
                    is DownloadProgress.InProgress -> {
                        val pct = if (progress.total > 0)
                            (progress.downloaded * 100L / progress.total).toInt()
                        else 0
                        _state.update { it.copy(model = ModelStatus.Downloading(pct)) }
                    }
                    DownloadProgress.Complete, DownloadProgress.Cached -> initModel(model, path)
                    is DownloadProgress.Failure -> {
                        _state.update { it.copy(model = ModelStatus.Failed(progress.message)) }
                        _effects.tryEmit(Effect.Main.ShowError(progress.message))
                    }
                }
            }
        }
    }

    private suspend fun initModel(model: LlamaModel, path: String) {
        _state.update { it.copy(model = ModelStatus.Initializing) }
        withContext(Dispatchers.Default) {
            val ok = llamaBridgeService.initModel(path)
            if (ok) {
                llamaBridgeService.updateParams(gpuEnabled = _state.value.gpuEnabled)
                _state.update { it.copy(model = ModelStatus.Ready) }
            } else {
                val msg = "モデルの初期化に失敗しました"
                _state.update { it.copy(model = ModelStatus.Failed(msg)) }
                _effects.tryEmit(Effect.Main.ShowError(msg))
            }
        }
    }

    private fun generate(prompt: String, turnId: String) {
        viewModelScope.launch {
            generateJob.onSome { job ->
                llamaBridgeService.cancelGenerate()
                job.join()
            }

            generateJob = launch {
                try {
                    withContext(Dispatchers.Default) {
                        llamaBridgeService.generateText(prompt).collect { result ->
                            result.leftOrNull()?.let { error ->
                                val msg = error.message
                                _state.update { state ->
                                    state.copy(
                                        turns = state.turns.map { t ->
                                            if (t.id == turnId) {
                                                t.copy(response = ResponseState.Failed(msg))
                                            } else t
                                        },
                                    )
                                }
                                _effects.tryEmit(Effect.Main.ShowError(msg))
                            }
                            result.getOrNull()?.let { gen ->
                                val nextResponse = when (gen) {
                                    is GenTextState.OnProgress -> ResponseState.Generating(gen.value)
                                    is GenTextState.Complete -> ResponseState.Completed(gen.value)
                                }
                                _state.update { state ->
                                    state.copy(
                                        turns = state.turns.map { t ->
                                            if (t.id == turnId) t.copy(response = nextResponse) else t
                                        },
                                    )
                                }
                            }
                        }
                    }
                } finally {
                    _state.update { state ->
                        state.copy(
                            turns = state.turns.map { t ->
                                val r = t.response
                                if (t.id == turnId && r is ResponseState.Generating) {
                                    t.copy(response = ResponseState.Completed(r.partial))
                                } else t
                            },
                        )
                    }
                }
            }.some()
        }
    }

    private fun generateTurnId(): String = Uuid.random().toString()
}
