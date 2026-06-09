package com.example.kmpllmdemonstration

import android.app.Application
import com.example.kmpllmdemonstration.di.commonModule
import com.example.kmpllmdemonstration.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KmpLlmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KmpLlmApp)
            modules(commonModule, platformModule)
        }
    }
}
