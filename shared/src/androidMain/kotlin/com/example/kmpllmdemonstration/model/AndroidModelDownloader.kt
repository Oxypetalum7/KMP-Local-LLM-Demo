package com.example.kmpllmdemonstration.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AndroidModelDownloader : ModelDownloader {
    override fun download(url: String, destinationPath: String): Flow<DownloadProgress> = flow {
        val destFile = File(destinationPath)
        if (destFile.exists() && destFile.length() > 0) {
            emit(DownloadProgress.Cached)
            return@flow
        }

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        val tempFile = File("$destinationPath.tmp")

        try {
            connection.connect()
            val code = connection.responseCode
            if (code !in 200..299) {
                emit(DownloadProgress.Failure("HTTP $code: ${connection.responseMessage ?: "サーバーエラー"}"))
                return@flow
            }

            val totalBytes = connection.contentLengthLong
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloaded = 0L
                    var bytes: Int
                    while (input.read(buffer).also { bytes = it } != -1) {
                        output.write(buffer, 0, bytes)
                        downloaded += bytes
                        emit(DownloadProgress.InProgress(downloaded, totalBytes))
                    }
                }
            }

            if (!tempFile.renameTo(destFile)) {
                tempFile.delete()
                emit(DownloadProgress.Failure("一時ファイルの移動に失敗しました"))
                return@flow
            }
            emit(DownloadProgress.Complete)
        } catch (e: Exception) {
            tempFile.delete()
            emit(DownloadProgress.Failure(e.message ?: "ダウンロードに失敗しました"))
        } finally {
            connection.disconnect()
        }
    }.flowOn(Dispatchers.IO)
}
