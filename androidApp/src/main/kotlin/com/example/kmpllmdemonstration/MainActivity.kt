package com.example.kmpllmdemonstration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.kmpllmdemonstration.di.commonModule
import com.example.kmpllmdemonstration.di.platformModule
import com.example.kmpllmdemonstration.navigation.AppNavGraph
import com.example.kmpllmdemonstration.ui.theme.AppTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KoinApplication(
                configuration = koinConfiguration(
                    declaration = {
                        androidContext(this@MainActivity)
                        modules(platformModule, commonModule)
                    }
                ),
                content = {
                    AppTheme {
                        Surface {
                            AppNavGraph()
                        }
                    }
                }
            )
        }
    }
}
