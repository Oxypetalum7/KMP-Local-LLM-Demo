package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModule)
    }
}

class IOSDependencies : KoinComponent {
    val llamaBridgeService: LlamaBridgeService by inject()
}
