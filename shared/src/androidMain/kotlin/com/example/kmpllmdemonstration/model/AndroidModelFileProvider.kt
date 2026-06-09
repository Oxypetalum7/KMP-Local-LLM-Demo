package com.example.kmpllmdemonstration.model

import android.content.Context
import java.io.File

class AndroidModelFileProvider(private val context: Context) : ModelFileProvider {
    override fun getFilePath(fileName: String): String =
        "${context.filesDir.absolutePath}/$fileName"

    override fun exists(fileName: String): Boolean =
        File(getFilePath(fileName)).exists()
}
