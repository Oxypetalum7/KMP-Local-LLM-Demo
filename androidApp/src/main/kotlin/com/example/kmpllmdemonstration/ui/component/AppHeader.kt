package com.example.kmpllmdemonstration.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmpllmdemonstration.viewModel.ChatUiState
import com.example.kmpllmdemonstration.viewModel.ModelStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    state: ChatUiState,
    onMenuTapped: () -> Unit,
    onLogoTapped: () -> Unit,
    onNewChatTapped: () -> Unit,
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                LogoButton(
                    isReady = state.isReady,
                    isTappable = state.isLogoTappable,
                    isDownloading = state.model is ModelStatus.Downloading,
                    isInitializing = state.isInitializing,
                    downloadPct = state.downloadProgressPct,
                    onTap = onLogoTapped,
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuTapped) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "メニュー",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            actions = {
                val newChatEnabled = state.turns.isNotEmpty() || state.input.isNotEmpty()
                IconButton(onClick = onNewChatTapped, enabled = newChatEnabled) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "新規会話",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun LogoButton(
    isReady: Boolean,
    isTappable: Boolean,
    isDownloading: Boolean,
    isInitializing: Boolean,
    downloadPct: Int?,
    onTap: () -> Unit,
) {
    val logoAlpha = if (isReady) 1f else 0.4f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clickable(enabled = isTappable, onClick = onTap),
    ) {
        when {
            isDownloading && downloadPct != null -> {
                CircularProgressIndicator(
                    progress = { downloadPct / 100f },
                    modifier = Modifier.size(46.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            isInitializing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(46.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = "🐺",
            fontSize = 24.sp,
            fontFamily = FontFamily.Default,
            modifier = Modifier.alpha(logoAlpha),
        )
    }
}
