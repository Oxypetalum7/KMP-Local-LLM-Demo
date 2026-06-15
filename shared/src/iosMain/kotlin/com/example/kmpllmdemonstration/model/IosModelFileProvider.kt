package com.example.kmpllmdemonstration.model

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
class IosModelFileProvider : ModelFileProvider {
    override fun getFilePath(fileName: String): String {
        val docUrl = NSFileManager.defaultManager.URLForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask,
            null,
            true,
            null,
        )
        return "${docUrl?.path}/$fileName"
    }

    override fun exists(fileName: String): Boolean =
        NSFileManager.defaultManager.fileExistsAtPath(getFilePath(fileName))
}
