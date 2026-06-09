package com.example.kmpllmdemonstration.model

interface ModelFileProvider {
    fun getFilePath(fileName: String): String
    fun exists(fileName: String): Boolean
}
