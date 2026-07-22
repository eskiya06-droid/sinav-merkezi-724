package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object FileHelper {

    fun getBase64FromImageUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Compress to prevent huge base64 strings
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Format needed by OpenAI/Nvidia: data:image/jpeg;base64,...
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getTextFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader()?.use { it.readText() }
            inputStream?.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
