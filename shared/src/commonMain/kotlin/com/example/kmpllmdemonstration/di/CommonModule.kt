package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.LlamaBridgeService
import org.koin.dsl.module

val commonModule = module {
    single<LlamaBridgeService>{ LlamaBridgeService() }
}