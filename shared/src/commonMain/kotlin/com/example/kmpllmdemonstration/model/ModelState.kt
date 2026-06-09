package com.example.kmpllmdemonstration.model

sealed class ModelState {
    data object Idle : ModelState()
    data class Downloading(val progressPct: Int) : ModelState()
    data object Initializing : ModelState()
    data class Ready(val model: LlamaModel) : ModelState()
    data class Error(val message: String) : ModelState()
}
