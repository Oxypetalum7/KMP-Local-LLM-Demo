package com.example.kmpllmdemonstration.viewModel

data class ChatUiState(
    val model: ModelStatus = ModelStatus.NotInitialized,
    val input: String = "",
    val turns: List<ChatTurn> = emptyList(),
    val isSidebarOpen: Boolean = false,
    val gpuEnabled: Boolean = false,
) {
    val isReady: Boolean get() = model is ModelStatus.Ready
    val isGenerating: Boolean
        get() = turns.lastOrNull()?.response is ResponseState.Generating
    val isInputEnabled: Boolean get() = isReady && !isGenerating
    val isSendVisible: Boolean get() = isInputEnabled && input.isNotBlank()
    val isLogoTappable: Boolean
        get() = model is ModelStatus.NotInitialized || model is ModelStatus.Failed
    val downloadProgressPct: Int?
        get() = (model as? ModelStatus.Downloading)?.pct
    val isInitializing: Boolean get() = model is ModelStatus.Initializing
}

sealed interface ModelStatus {
    data object NotInitialized : ModelStatus
    data class Downloading(val pct: Int) : ModelStatus
    data object Initializing : ModelStatus
    data object Ready : ModelStatus
    data class Failed(val message: String) : ModelStatus
}

data class ChatTurn(
    val id: String,
    val prompt: String,
    val response: ResponseState,
    val isPromptCollapsed: Boolean = true,
)

sealed interface ResponseState {
    data class Generating(val partial: String) : ResponseState
    data class Completed(val text: String) : ResponseState
    data class Failed(val message: String) : ResponseState
}
