package com.example.kmpllmdemonstration.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpllmdemonstration.viewModel.ChatTurn
import com.example.kmpllmdemonstration.viewModel.ResponseState

@Composable
fun TurnItem(
    turn: ChatTurn,
    showDivider: Boolean,
    onTogglePromptCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PromptBubble(
            prompt = turn.prompt,
            collapsed = turn.isPromptCollapsed,
            onClick = onTogglePromptCollapse,
            modifier = Modifier.align(Alignment.End),
        )
        when (val r = turn.response) {
            is ResponseState.Generating -> GeneratingResponse(partial = r.partial)
            is ResponseState.Completed -> CompletedResponse(text = r.text)
            is ResponseState.Failed -> FailedResponse(message = r.message)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun GeneratingResponse(partial: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (partial.isNotEmpty()) {
            Text(
                text = partial,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompletedResponse(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun FailedResponse(message: String) {
    Text(
        text = "応答エラー: $message",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}
