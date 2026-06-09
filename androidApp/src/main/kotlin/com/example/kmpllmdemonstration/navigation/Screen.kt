package com.example.kmpllmdemonstration.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
}
