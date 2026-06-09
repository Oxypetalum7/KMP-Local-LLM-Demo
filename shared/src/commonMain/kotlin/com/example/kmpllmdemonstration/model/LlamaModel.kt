package com.example.kmpllmdemonstration.model

sealed class LlamaModel(
    val fileName: String,
    val displayName: String,
    val downloadUrl: String,
    val sizeLabel: String,
) {
    data object Gemma4E4BQ2 : LlamaModel(
        fileName = "gemma-4-E2B_q4_0-it.gguf",
        displayName = "Gemma 4 E4B",
        downloadUrl = "https://huggingface.co/google/gemma-4-E2B-it-qat-q4_0-gguf/resolve/main/gemma-4-E2B_q4_0-it.gguf",
        sizeLabel = "3.2 GB",
    )

    companion object {
        val all: List<LlamaModel> = listOf(Gemma4E4BQ2)
    }
}
