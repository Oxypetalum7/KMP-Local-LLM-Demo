package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single<LlamaBridgeService> { LlamaBridgeService() }
    viewModel { MainViewModel(get(), get(), get()) }
}