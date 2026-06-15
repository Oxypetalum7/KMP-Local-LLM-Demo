package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import com.example.kmpllmdemonstration.model.KtorModelDownloader
import com.example.kmpllmdemonstration.model.ModelDownloader
import com.example.kmpllmdemonstration.model.createHttpClient
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single<LlamaBridgeService> { LlamaBridgeService() }
    single<HttpClient> { createHttpClient() }
    single<ModelDownloader> { KtorModelDownloader(get()) }
    viewModel { MainViewModel(get(), get(), get()) }
}
