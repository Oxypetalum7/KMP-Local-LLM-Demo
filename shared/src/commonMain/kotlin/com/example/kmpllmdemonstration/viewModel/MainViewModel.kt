package com.example.kmpllmdemonstration.viewModel

import androidx.lifecycle.ViewModel
import com.example.kmpllmdemonstration.LlamaBridgeService
import kotlinx.coroutines.CoroutineScope

class MainViewModel(
    private val coroutineScope: CoroutineScope,
    private val llamaBridgeService: LlamaBridgeService
) : ViewModel() {

}