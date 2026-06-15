package com.example.kmpllmdemonstration.model

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.write

class KtorModelDownloader(
    private val client: HttpClient,
) : ModelDownloader {
    override fun download(url: String, destinationPath: String): Flow<DownloadProgress> = flow {
        val collector = this
        val destPath = Path(destinationPath)
        val tmpPath = Path("$destinationPath.tmp")

        // キャッシュ確認：完成ファイルがあれば再DL不要
        val destMeta = SystemFileSystem.metadataOrNull(destPath)
        if (destMeta != null && destMeta.size > 0) {
            emit(DownloadProgress.Cached)
            return@flow
        }

        // 前回中断分のテンポラリを削除
        if (SystemFileSystem.exists(tmpPath)) {
            SystemFileSystem.delete(tmpPath)
        }

        // 開始通知
        emit(DownloadProgress.InProgress(0L, 0L))

        try {
            client.prepareGet(url).execute { response ->
                if (!response.status.isSuccess()) {
                    collector.emit(DownloadProgress.Failure("HTTP ${response.status.value}"))
                    return@execute
                }

                val total = response.contentLength() ?: 0L
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(64 * 1024)
                var downloaded = 0L

                SystemFileSystem.sink(tmpPath).buffered().use { sink ->
                    while (true) {
                        val n = channel.readAvailable(buffer, 0, buffer.size)
                        if (n == -1) break
                        if (n > 0) {
                            sink.write(buffer, 0, n)
                            downloaded += n
                            collector.emit(DownloadProgress.InProgress(downloaded, total))
                        }
                    }
                    sink.flush()
                }

                // ストリーミング完了 → 目的地へ atomic move
                SystemFileSystem.atomicMove(tmpPath, destPath)
                collector.emit(DownloadProgress.Complete)
            }
        } catch (e: Exception) {
            if (SystemFileSystem.exists(tmpPath)) {
                runCatching { SystemFileSystem.delete(tmpPath) }
            }
            emit(DownloadProgress.Failure(e.message ?: "ダウンロードに失敗しました"))
        }
    }.flowOn(Dispatchers.Default)
}
