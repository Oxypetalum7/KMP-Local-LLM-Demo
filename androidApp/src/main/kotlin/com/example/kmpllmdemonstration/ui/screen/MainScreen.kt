package com.example.kmpllmdemonstration.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpllmdemonstration.model.LlamaModel
import com.example.kmpllmdemonstration.model.ModelState
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>()
    val modelState by viewModel.modelState.collectAsStateWithLifecycle()
    val generatedText by viewModel.generatedText.collectAsStateWithLifecycle()
    val gpuEnabled by viewModel.gpuEnabled.collectAsStateWithLifecycle()

    MainContent(
        modelState = modelState,
        generatedText = generatedText,
        gpuEnabled = gpuEnabled,
        onDownloadClick = { viewModel.downloadAndInit(LlamaModel.Gemma4E4BQ2) },
        onGenerateClick = { viewModel.generate(it) },
        onGpuToggle = { viewModel.toggleGpu(it) },
    )
}

@Composable
private fun MainContent(
    modelState: ModelState,
    generatedText: String,
    gpuEnabled: Boolean,
    onDownloadClick: () -> Unit,
    onGenerateClick: (String) -> Unit,
    onGpuToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "KMP LLM Demo",
            style = MaterialTheme.typography.headlineSmall,
        )

        when (modelState) {
            ModelState.Idle -> IdleSection(onDownloadClick)
            is ModelState.Downloading -> DownloadingSection(modelState.progressPct)
            ModelState.Initializing -> InitializingSection()
            is ModelState.Ready -> ReadySection(
                model = modelState.model,
                generatedText = generatedText,
                gpuEnabled = gpuEnabled,
                onGenerateClick = onGenerateClick,
                onGpuToggle = onGpuToggle,
            )
            is ModelState.Error -> ErrorSection(
                message = modelState.message,
                onRetry = onDownloadClick,
            )
        }
    }
}

@Composable
private fun IdleSection(onDownloadClick: () -> Unit) {
    val model = LlamaModel.Gemma4E4BQ2
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = model.displayName, style = MaterialTheme.typography.bodyLarge)
        Text(text = "サイズ: ${model.sizeLabel}", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onDownloadClick, modifier = Modifier.fillMaxWidth()) {
            Text("ダウンロードして読み込む")
        }
    }
}

@Composable
private fun DownloadingSection(progressPct: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("ダウンロード中... $progressPct%")
        LinearProgressIndicator(
            progress = { progressPct / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InitializingSection() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator()
            Text("モデルを初期化中...")
        }
    }
}

@Composable
private fun ReadySection(
    model: LlamaModel,
    generatedText: String,
    gpuEnabled: Boolean,
    onGenerateClick: (String) -> Unit,
    onGpuToggle: (Boolean) -> Unit,
) {
    var prompt by rememberSaveable { mutableStateOf("自己紹介してください。") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${model.displayName} 準備完了",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "GPU アクセラレーション",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = gpuEnabled,
                onCheckedChange = onGpuToggle,
            )
        }
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("プロンプト") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Button(
            onClick = { onGenerateClick(prompt) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("生成")
        }
        if (generatedText.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = generatedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "エラー: $message",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("再試行")
        }
    }
}
