package com.speedbump

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.LinearLayout
import android.view.Gravity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            
            val rootLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(50, 50, 50, 50)
            }

        val overlayButton = Button(this).apply {
            text = "Enable Overlay Permission"
            setOnClickListener {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Could not open settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val usageButton = Button(this).apply {
            text = "Enable Usage Access"
            setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Could not open settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        rootLayout.addView(overlayButton)
        rootLayout.addView(usageButton)
        
        setContentView(rootLayout)

        // Start the monitor service
        try {
            val serviceIntent = Intent(this, SpeedbumpMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("Speedbump", "Service Start Error: ${e.message}")
        }
        } catch (e: Exception) {
            android.util.Log.e("Speedbump", "MainActivity Crash: ${e.message}")
            finish()
        }
    }
}
