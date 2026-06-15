package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.model.IosModelFileProvider
import com.example.kmpllmdemonstration.model.ModelFileProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ModelFileProvider> { IosModelFileProvider() }
}
