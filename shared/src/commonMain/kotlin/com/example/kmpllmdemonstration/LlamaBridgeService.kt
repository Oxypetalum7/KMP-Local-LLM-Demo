package com.example.kmpllmdemonstration

import arrow.core.Ior
import arrow.core.leftIor
import arrow.core.rightIor
import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LlamaBridgeService {
    private var modelPath: String? = null

    fun initModel(modelName: String) {
        modelPath = LlamaBridge.getModelPath(modelFileName = modelName)
    }

    fun generateText(prompt: String): Flow<Ior<GenerateTextInterruptedException, GenTextState>> = callbackFlow {
        val textBuffer = StringBuilder()
        trySend(GenTextState.OnProgress("").rightIor())

        LlamaBridge.generateStream(
            prompt = prompt,
            callback = object : GenStream {
                override fun onDelta(text: String) {
                    textBuffer.append(text)
                    trySend(GenTextState.OnProgress(textBuffer.toString()).rightIor())
                }

                override fun onComplete() {
                    close()
                }

                override fun onError(message: String) {
                    trySend(GenerateTextInterruptedException(message).leftIor())
                    close()
                }
            }
        )

        awaitClose {}
    }
}

data class GenerateTextInterruptedException(override val message: String) : Exception()

sealed class GenTextState(open val value: String) {
    data class OnProgress(override val value: String) : GenTextState(value = value)
    data class Complete(override val value: String) : GenTextState(value = value)
}