package com.speedbump

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class UpdateManager(private val context: Context) {

    private val VERSION_URL = "https://raw.githubusercontent.com/Sahi-KK/ecommerce-speedbump/main/version.json"
    private val APK_URL = "https://github.com/Sahi-KK/ecommerce-speedbump/releases/latest/download/app-debug.apk"

    fun checkForUpdates() {
        thread {
            try {
                val connection = URL(VERSION_URL).openConnection() as HttpURLConnection
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val remoteVersionCode = json.getInt("versionCode")
                val remoteVersionName = json.getString("versionName")

                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val localVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode
                }

                if (remoteVersionCode > localVersionCode) {
                    Handler(Looper.getMainLooper()).post {
                        showUpdateDialog(remoteVersionName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showUpdateDialog(newVersion: String) {
        AlertDialog.Builder(context)
            .setTitle("New Update Available")
            .setMessage("Version $newVersion is available. Download and install now?")
            .setPositiveButton("Update") { _, _ ->
                startDownload()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun startDownload() {
        thread {
            try {
                val url = URL(APK_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val file = File(context.externalCacheDir, "update.apk")
                val outputStream = FileOutputStream(file)
                val inputStream = connection.inputStream

                val buffer = ByteArray(1024)
                var len = inputStream.read(buffer)
                while (len != -1) {
                    outputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }

                outputStream.close()
                inputStream.close()

                Handler(Looper.getMainLooper()).post {
                    installApk(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun installApk(file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
