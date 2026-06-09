package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.viewmodel.scope.viewModelScope

@OptIn(KoinExperimentalAPI::class, KoinViewModelScopeApi::class)
val commonModule = module {
    // singleton
    single<LlamaBridgeService> { LlamaBridgeService() }

    // viewModel
    viewModelScope {
        viewModel { MainViewModel(get(), get()) }
    }
}