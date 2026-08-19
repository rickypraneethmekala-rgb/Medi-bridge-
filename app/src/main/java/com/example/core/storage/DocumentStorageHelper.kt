package com.example.core.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object DocumentStorageHelper {

    private fun getDocumentsDir(context: Context): File {
        val dir = File(context.filesDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getPhotosDir(context: Context): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createTempCameraImageUri(context: Context): Pair<Uri, String> {
        val dir = getPhotosDir(context)
        val file = File(dir, "temp_camera_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Pair(uri, file.absolutePath)
    }

    fun saveFileToPrivateStorage(context: Context, sourceUri: Uri, fileExtension: String): Pair<String, String>? {
        return try {
            val dir = getDocumentsDir(context)
            val docId = UUID.randomUUID().toString()
            val ext = if (fileExtension.startsWith(".")) fileExtension else ".$fileExtension"
            val targetFile = File(dir, "doc_${docId}$ext")

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return null

            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            Pair(docId, targetFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePrivateFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openPdfWithViewer(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Open Medical PDF"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
