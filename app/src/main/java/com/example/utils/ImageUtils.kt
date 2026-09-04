package com.example.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    // Resize bitmap if too large to save API limits and time
    val maxDimension = 1024
    val ratio: Float = maxDimension.toFloat() / Math.max(this.width, this.height)
    val scaledBitmap = if (ratio < 1) {
        Bitmap.createScaledBitmap(this, (this.width * ratio).toInt(), (this.height * ratio).toInt(), true)
    } else {
        this
    }
    
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
