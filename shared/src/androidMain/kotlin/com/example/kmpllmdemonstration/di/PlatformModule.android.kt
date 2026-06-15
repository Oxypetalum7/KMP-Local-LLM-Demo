package com.example.kmpllmdemonstration.di

import com.example.kmpllmdemonstration.model.AndroidModelFileProvider
import com.example.kmpllmdemonstration.model.ModelFileProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ModelFileProvider> { AndroidModelFileProvider(androidContext()) }
}
