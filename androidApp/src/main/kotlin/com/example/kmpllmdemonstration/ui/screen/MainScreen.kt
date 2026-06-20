package com.example.kmpllmdemonstration.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpllmdemonstration.ui.component.AppHeader
import com.example.kmpllmdemonstration.ui.component.ChatDrawer
import com.example.kmpllmdemonstration.ui.component.ChatInputBar
import com.example.kmpllmdemonstration.ui.component.TurnItem
import com.example.kmpllmdemonstration.ui.util.fadingEdgeBottom
import com.example.kmpllmdemonstration.viewModel.ChatUiState
import com.example.kmpllmdemonstration.viewModel.Effect
import com.example.kmpllmdemonstration.viewModel.Intent
import com.example.kmpllmdemonstration.viewModel.MainViewModel
import com.example.kmpllmdemonstration.viewModel.ResponseState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>()
val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSidebarOpen) {
        if (state.isSidebarOpen && !drawerState.isOpen) drawerState.open()
        if (!state.isSidebarOpen && drawerState.isOpen) drawerState.close()
    }
    LaunchedEffect(drawerState.currentValue) {
        val isOpen = drawerState.currentValue == DrawerValue.Open
        if (isOpen != state.isSidebarOpen) {
            viewModel.dispatch(Intent.Main.ToggleSidebar)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is Effect.Main.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                gpuEnabled = state.gpuEnabled,
                onToggleGpu = { viewModel.dispatch(Intent.Setting.ToggleGpu(it)) },
            )
        },
    ) {
        Scaffold(
            topBar = {
                AppHeader(
                    state = state,
                    onMenuTapped = { viewModel.dispatch(Intent.Main.ToggleSidebar) },
                    onLogoTapped = { viewModel.dispatch(Intent.Main.LogoTapped) },
                    onNewChatTapped = { viewModel.dispatch(Intent.Main.StartNewChat) },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            ChatBody(
                state = state,
                paddingValues = paddingValues,
                onInputChange = { viewModel.dispatch(Intent.Main.UpdateInput(it)) },
                onSend = { viewModel.dispatch(Intent.Main.Send) },
                onTogglePromptCollapse = {
                    viewModel.dispatch(Intent.Main.TogglePromptCollapse(it))
                },
            )
        }
    }
}

@Composable
private fun ChatBody(
    state: ChatUiState,
    paddingValues: PaddingValues,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onTogglePromptCollapse: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val lastTurnId = state.turns.lastOrNull()?.id
    val lastResponse = (state.turns.lastOrNull()?.response as? ResponseState.Generating)?.partial

    LaunchedEffect(lastTurnId) {
        if (state.turns.isNotEmpty()) {
            listState.animateScrollToItem(state.turns.size - 1)
        }
    }
    LaunchedEffect(lastResponse) {
        if (state.turns.isNotEmpty()) {
            listState.animateScrollToItem(state.turns.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            // paddingValues に含まれるナビバー inset を消費済みとして扱い、
            // imePadding との二重加算を防ぐ（入力バーをキーボード上端にぴったり寄せる）
            .consumeWindowInsets(paddingValues)
            .imePadding(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .fadingEdgeBottom(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                itemsIndexed(state.turns, key = { _, t -> t.id }) { index, turn ->
                    TurnItem(
                        turn = turn,
                        showDivider = index < state.turns.size - 1,
                        onTogglePromptCollapse = { onTogglePromptCollapse(turn.id) },
                    )
                }
            }
        }
        ChatInputBar(
            input = state.input,
            enabled = state.isInputEnabled,
            isSendVisible = state.isSendVisible,
            onInputChange = onInputChange,
            onSend = onSend,
        )
    }
}
