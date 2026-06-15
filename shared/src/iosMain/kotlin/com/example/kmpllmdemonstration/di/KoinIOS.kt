package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModule, platformModule)
    }
}

class IOSDependencies : KoinComponent {
    val llamaBridgeService: LlamaBridgeService by inject()
    val chatViewModel: MainViewModel by inject()
}
