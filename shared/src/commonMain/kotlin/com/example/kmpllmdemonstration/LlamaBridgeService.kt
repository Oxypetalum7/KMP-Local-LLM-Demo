package com.example.kmpllmdemonstration

import arrow.core.Ior
import arrow.core.leftIor
import arrow.core.rightIor
import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import com.llamatik.library.platform.LlamaSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.concurrent.Volatile

class LlamaBridgeService {
    private var modelPath: String? = null

    // session.cancel() は stream() 実行中に呼ぶ想定のため @Volatile で可視性を保証
    @Volatile
    private var activeSession: LlamaSession? = null

    fun initModel(path: String): Boolean {
        val resolvedPath = LlamaBridge.getModelPath(modelFileName = path)
        val ok = LlamaBridge.initGenerateModel(resolvedPath)
        if (ok) modelPath = resolvedPath
        return ok
    }

    fun updateParams(gpuEnabled: Boolean) {
        LlamaBridge.updateGenerateParams(
            temperature = 0.7f,
            maxTokens = 512,
            topP = 0.9f,
            topK = 40,
            repeatPenalty = 1.1f,
            contextLength = 2048,
            numThreads = 4,
            useMmap = true,
            flashAttention = gpuEnabled,
            batchSize = 512,
            gpuLayers = if (gpuEnabled) -1 else 0,
        )
    }

    // session.cancel() → onError コールバック → channel.close() の順序で自然に止まる
    fun cancelGenerate() {
        activeSession?.cancel()
    }

    fun generateText(prompt: String): Flow<Ior<GenerateTextInterruptedException, GenTextState>> = callbackFlow {
        val session = LlamaBridge.createSession(name = "generate")
            ?: run {
                trySend(GenerateTextInterruptedException("セッションの作成に失敗しました（モデル未ロード）").leftIor())
                close()
                return@callbackFlow
            }

        activeSession = session
        val textBuffer = StringBuilder()
        trySend(GenTextState.OnProgress("").rightIor())

        session.stream(
            prompt = prompt,
            callback = object : GenStream {
                override fun onDelta(text: String) {
                    textBuffer.append(text)
                    trySend(GenTextState.OnProgress(textBuffer.toString()).rightIor())
                }

                override fun onComplete() {
                    trySend(GenTextState.Complete(textBuffer.toString()).rightIor())
                    close()
                }

                override fun onError(message: String) {
                    // ユーザーキャンセルも onError 経由で来るため channel を閉じるのみ
                    close()
                }
            }
        )

        // stream() がブロッキングで返った後にリソース解放
        // close() が呼ばれた後に awaitClose が実行される
        awaitClose {
            session.close()
            activeSession = null
        }
    }
}

data class GenerateTextInterruptedException(override val message: String) : Exception()

sealed class GenTextState(open val value: String) {
    data class OnProgress(override val value: String) : GenTextState(value = value)
    data class Complete(override val value: String) : GenTextState(value = value)
}
