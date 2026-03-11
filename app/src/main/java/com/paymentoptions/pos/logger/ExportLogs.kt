package com.paymentoptions.pos.logger

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportLogs {
    private const val LOG_FOLDER = "daspay"
    private const val LOG_ZIP_FOLDER = "daspay_logs_zip"

    /** Zips the log files into a folder with the provided zip file name. */
    private fun zipLogFolder(context: Context, filePath: Uri): Boolean {
        val inputDirectory = File(context.cacheDir, LOG_FOLDER)
        val fileDescriptor = context.contentResolver.openFileDescriptor(filePath, "w")
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(fileDescriptor?.fileDescriptor)))
                .use { zos ->
                    inputDirectory.walkTopDown().forEach { file ->
                        val zipFileName =
                            file.absolutePath
                                .removePrefix(inputDirectory.absolutePath)
                                .removePrefix("/")
                        val entry = ZipEntry("$zipFileName${(if (file.isDirectory) "/" else "")}")
                        zos.putNextEntry(entry)
                        if (file.isFile) {
                            file.inputStream().copyTo(zos)
                        }
                    }
                }
            return true
        } catch (e: IOException) {
            Log.e("zipLogFolder()", e.message.toString())
        } finally {
            fileDescriptor?.close()
        }
        return false
    }

    private fun deleteExistingLogsFolder(context: Context) {
        val logsZipFolder = File(context.getExternalFilesDir(null), LOG_ZIP_FOLDER)
        if (logsZipFolder.exists()) {
            logsZipFolder.deleteRecursively()
        }
    }

    private fun exportLogsToExternalStorage(context: Context, filePath: Uri): Boolean =
        zipLogFolder(context, filePath)

    fun sendLogsToSdkTeam(context: Context) {
        val fileUri = getFileUri(context)
        val export = exportLogsToExternalStorage(context, fileUri)
        if (export) {
            openEmailChooser(context, File(fileUri.path!!))
        }
    }

    private fun getFileUri(context: Context): Uri {
        deleteExistingLogsFolder(context)
        val destFile = File(context.getExternalFilesDir(null), LOG_ZIP_FOLDER)
        if (!destFile.exists()) {
            destFile.mkdirs()
        }
        val file = File(destFile, "log-${System.currentTimeMillis()}.zip")
        return Uri.fromFile(file)
    }

    private fun openEmailChooser(context: Context, file: File) {
        val fileUri = provideUriPermission(context, file)
        AppLogger.debug(
            "sendlog.sendLogsToSdkTeam to path = ${file.path}, fileSize = ${
                File(
                    file.path,
                ).length()
            } " +
                "package Name ${context.packageName}",
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share Logs Daspay app")
        intent.putExtra(Intent.EXTRA_TEXT, "Daspay Logs")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            context.startActivity(Intent.createChooser(intent, "Send Logs..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
        file.deleteOnExit()
    }

    fun provideUriPermission(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
