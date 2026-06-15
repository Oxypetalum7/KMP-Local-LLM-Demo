package com.example.kmpllmdemonstration.model

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    followRedirects = true
    install(HttpTimeout) {
        requestTimeoutMillis = 60 * 60 * 1000  // 1 hour for large model files
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 60_000
    }
}
