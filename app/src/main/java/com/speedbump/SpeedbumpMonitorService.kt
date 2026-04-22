package com.speedbump

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class SpeedbumpMonitorService : Service() {

    private lateinit var overlay: SpeedbumpOverlay
    private val handler = Handler(Looper.getMainLooper())
    private var lastTriggeredPackage = ""
    private var lastTriggeredTime = 0L

    private val SHOPPING_PACKAGES = setOf(
        "com.amazon.mShop.android.shopping",
        "in.amazon.mShop.android.shopping",
        "com.flipkart.android",
        "com.meesho.supply",
        "com.myntra.android",
        "com.ril.ajio",
        "com.fsn.nykaa",
        "com.tul.tatacliq",
        "com.jio.jiomart",
        "com.tatadigital.tcp",
        "com.grofers.customerapp",
        "com.zeptoconsumerapp",
        "com.bigbasket.mobileapp",
        "com.dunzo.user",
        "in.swiggy.android",
        "com.application.zomato"
    )

    override fun onCreate() {
        super.onCreate()
        overlay = SpeedbumpOverlay(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, createNotification())
        }
        startMonitoring()
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkForegroundApp()
                handler.postDelayed(this, 1500) // Check every 1.5 seconds
            }
        })
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)

        if (stats != null) {
            val topApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""
            
            if (topApp in SHOPPING_PACKAGES) {
                // Prevent spamming if already triggered recently for this app (e.g. within 5 mins)
                if (topApp != lastTriggeredPackage || (time - lastTriggeredTime > 5 * 60 * 1000)) {
                    Log.d("Speedbump", "Shopping app detected: $topApp")
                    lastTriggeredPackage = topApp
                    lastTriggeredTime = time
                    overlay.show(0.0) // 0.0 because we can't read the price now
                }
            } else if (topApp != packageName && topApp != "") {
                // If they are in a non-shopping app, reset the "last triggered" so it can trigger again when they return
                // But we don't want to reset it immediately while they are in the shopping app
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "speedbump_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Speedbump Monitor", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        })
            .setContentTitle("Speedbump Active")
            .setContentText("Monitoring shopping apps...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
