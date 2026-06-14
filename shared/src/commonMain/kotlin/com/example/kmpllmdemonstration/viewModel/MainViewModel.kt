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
import com.example.kmpllmdemonstration.model.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val llamaBridgeService: LlamaBridgeService,
    private val modelFileProvider: ModelFileProvider,
    private val modelDownloader: ModelDownloader,
) : ViewModel() {

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Idle)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private val _generatedText = MutableStateFlow("")
    val generatedText: StateFlow<String> = _generatedText.asStateFlow()

    private val _gpuEnabled = MutableStateFlow(false)
    val gpuEnabled: StateFlow<Boolean> = _gpuEnabled.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var generateJob: Option<Job> = none()

    init {
        // アプリ再起動後にキャッシュ済みモデルを自動検出してロードする
        viewModelScope.launch {
            val model = LlamaModel.all.firstOrNull() ?: return@launch
            if (modelFileProvider.exists(model.fileName)) {
                initModel(model, modelFileProvider.getFilePath(model.fileName))
            }
        }
    }

    fun toggleGpu(enabled: Boolean) {
        _gpuEnabled.value = enabled
        viewModelScope.launch(Dispatchers.Default) {
            llamaBridgeService.updateParams(gpuEnabled = enabled)
        }
    }

    fun downloadAndInit(model: LlamaModel) {
        viewModelScope.launch {
            val path = modelFileProvider.getFilePath(model.fileName)
            modelDownloader.download(model.downloadUrl, path).collect { progress ->
                when (progress) {
                    is DownloadProgress.InProgress -> {
                        val pct = if (progress.total > 0)
                            (progress.downloaded * 100L / progress.total).toInt()
                        else 0
                        _modelState.value = ModelState.Downloading(pct)
                    }
                    DownloadProgress.Complete -> initModel(model, path)
                    DownloadProgress.Cached -> initModel(model, path)
                    is DownloadProgress.Failure -> _modelState.value =
                        ModelState.Error(progress.message)
                }
            }
        }
    }

    private suspend fun initModel(model: LlamaModel, path: String) {
        _modelState.value = ModelState.Initializing
        withContext(Dispatchers.Default) {
            val ok = llamaBridgeService.initModel(path)
            if (ok) {
                llamaBridgeService.updateParams(gpuEnabled = _gpuEnabled.value)
                _modelState.value = ModelState.Ready(model)
            } else {
                _modelState.value = ModelState.Error("モデルの初期化に失敗しました")
            }
        }
    }

    fun generate(prompt: String) {
        val currentModel = (_modelState.value as? ModelState.Ready)?.model ?: return
        viewModelScope.launch {
            // ネイティブセッションを止めてから前の Job が完全に終わるまで待つ
            generateJob.onSome { job ->
                llamaBridgeService.cancelGenerate()
                job.join()
            }

            // キャンセル由来のエラー状態をリセット
            _modelState.value = ModelState.Ready(currentModel)
            _generatedText.value = ""
            _isGenerating.value = true

            generateJob = launch {
                withContext(Dispatchers.Default) {
                    llamaBridgeService.generateText(prompt).collect { result ->
                        result.fold(
                            fa = { error ->
                                _modelState.value = ModelState.Error(error.message)
                                _isGenerating.value = false
                            },
                            fb = { state ->
                                _generatedText.value = state.value
                                if (state is GenTextState.Complete) _isGenerating.value = false
                            },
                            fab = { _, state ->
                                _generatedText.value = state.value
                                if (state is GenTextState.Complete) _isGenerating.value = false
                            },
                        )
                    }
                }
                // flow が正常終了したが Complete が来なかった場合（キャンセル等）のフォールバック
                _isGenerating.value = false
            }.some()
        }
    }
}
