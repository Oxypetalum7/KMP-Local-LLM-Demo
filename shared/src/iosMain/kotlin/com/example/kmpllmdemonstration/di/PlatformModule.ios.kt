package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.model.DownloadProgress
import com.example.kmpllmdemonstration.model.IosModelFileProvider
import com.example.kmpllmdemonstration.model.ModelDownloader
import com.example.kmpllmdemonstration.model.ModelFileProvider
import kotlinx.coroutines.flow.flow
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ModelFileProvider> { IosModelFileProvider() }
    single<ModelDownloader> {
        ModelDownloader { _, _ ->
            flow { emit(DownloadProgress.Failure("iOS ダウンロードは未実装です")) }
        }
    }
}