package com.example.kmpllmdemonstration.model

sealed class LlamaModel(
    val fileName: String,
    val displayName: String,
    val downloadUrl: String,
    val sizeLabel: String,
) {
    data object Gemma4E4BQ2 : LlamaModel(
        fileName = "google_gemma-4-E2B-it-Q4_K_S.gguf",
        displayName = "Gemma 4 E2B (Q4_K_S)",
        downloadUrl = "https://huggingface.co/bartowski/google_gemma-4-E2B-it-GGUF/resolve/main/google_gemma-4-E2B-it-Q4_K_S.gguf",
        sizeLabel = "3.15 GB",
    )

    companion object {
        val all: List<LlamaModel> = listOf(Gemma4E4BQ2)
    }
}
