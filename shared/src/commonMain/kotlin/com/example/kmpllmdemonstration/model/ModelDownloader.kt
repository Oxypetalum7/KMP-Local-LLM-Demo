package com.example.kmpllmdemonstration.model

import kotlinx.coroutines.flow.Flow

fun interface ModelDownloader {
    fun download(url: String, destinationPath: String): Flow<DownloadProgress>
}

sealed class DownloadProgress {
    data class InProgress(val downloaded: Long, val total: Long) : DownloadProgress()
    data object Complete : DownloadProgress()
    data object Cached : DownloadProgress()
    data class Failure(val message: String) : DownloadProgress()
}
