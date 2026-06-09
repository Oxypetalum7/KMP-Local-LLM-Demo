package com.example.kmpllmdemonstration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.kmpllmdemonstration.di.commonModule
import com.example.kmpllmdemonstration.navigation.AppNavGraph
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LlmDemoApp()
        }
    }
}

@Composable
fun LlmDemoApp() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = { modules(commonModule) }
        ),
        content = {
            MaterialTheme {
                Surface {
                    AppNavGraph()
                }
            }
        }
    )
}

@Preview
@Composable
fun LlmDemoAppPreview() {
    LlmDemoApp()
}
