package com.example.kmpllmdemonstration.viewModel

sealed interface Effect {
    sealed interface Main : Effect {
        data class ShowError(val message: String) : Main
    }
}
