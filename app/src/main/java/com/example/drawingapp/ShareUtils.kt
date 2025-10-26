package com.example.drawingapp

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/*
Used for sharing drawings with other apps on the OS. Uses the filepath, creates a URI, and creates
an Intent including that URI
 */
fun shareImageFile(context: Context, absolutePath: String) {
    val f = File(absolutePath)
    if (!f.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        f
    )

    val mime = when (f.extension.lowercase()) {
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        else -> "image/*"
    }

    val send = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Share drawing"))
}
