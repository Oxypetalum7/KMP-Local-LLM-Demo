package com.example.kmpllmdemonstration

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform