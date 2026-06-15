package com.example.kmpllmdemonstration.model

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession

class IosModelDownloader : ModelDownloader {
    override fun download(url: String, destinationPath: String): Flow<DownloadProgress> = callbackFlow {
        // キャッシュ確認：ファイルが存在してサイズ > 0 なら再ダウンロード不要
        if (NSFileManager.defaultManager.fileExistsAtPath(destinationPath)) {
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(destinationPath, null)
            val size = (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
            if (size > 0L) {
                trySend(DownloadProgress.Cached)
                close()
                return@callbackFlow
            }
        }

        val nsUrl = NSURL.URLWithString(url) ?: run {
            trySend(DownloadProgress.Failure("無効な URL: $url"))
            close()
            return@callbackFlow
        }

        // 前回の中断ファイルを削除
        NSFileManager.defaultManager.removeItemAtPath("$destinationPath.tmp", null)

        // ダウンロード開始通知（iOS は NSURLSessionDownloadDelegate なしでは中間進捗不可のため total=0）
        trySend(DownloadProgress.InProgress(0L, 0L))

        val task = NSURLSession.sharedSession.downloadTaskWithURL(nsUrl) { tmpUrl, response, error ->
            if (error != null) {
                trySend(DownloadProgress.Failure(error.localizedDescription))
                close()
                return@downloadTaskWithURL
            }
            // HTTP ステータスコード確認
            val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: -1
            if (statusCode !in 200..299) {
                trySend(DownloadProgress.Failure("HTTP $statusCode"))
                close()
                return@downloadTaskWithURL
            }
            val srcPath = tmpUrl?.path ?: run {
                trySend(DownloadProgress.Failure("ダウンロード一時ファイルが見つかりません"))
                close()
                return@downloadTaskWithURL
            }
            // 一時ファイルを目的地パスへ移動
            val moved = NSFileManager.defaultManager.moveItemAtPath(srcPath, destinationPath, null)
            trySend(
                if (moved) DownloadProgress.Complete
                else DownloadProgress.Failure("ファイルの移動に失敗しました")
            )
            close()
        }

        task.resume()

        awaitClose { task.cancel() }
    }
}
