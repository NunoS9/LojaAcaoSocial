package com.ipca.sassolidaria.utils

import android.content.Context
import android.net.Uri
import android.util.Base64

fun uriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Não foi possível abrir o ficheiro.")

    val bytes = inputStream.readBytes()
    inputStream.close()

    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun base64ToBytes(base64: String): ByteArray {
    return Base64.decode(base64, Base64.NO_WRAP)
}
