package com.mobile.tugasrancangmoka.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object UriToFileUtil {
    fun getFileFromUri(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)

        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}