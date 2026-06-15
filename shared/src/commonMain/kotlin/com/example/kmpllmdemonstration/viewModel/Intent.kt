package com.example.kmpllmdemonstration.viewModel

sealed interface Intent {
    sealed interface Main : Intent {
        data class UpdateInput(val text: String) : Main
        data object Send : Main
        data object LogoTapped : Main
        data class TogglePromptCollapse(val turnId: String) : Main
        data object StartNewChat : Main
        data object ToggleSidebar : Main
        data object DismissError : Main
    }

    sealed interface Setting : Intent {
        data class ToggleGpu(val enabled: Boolean) : Setting
    }
}
