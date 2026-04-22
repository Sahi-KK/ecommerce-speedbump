package com.speedbump

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var interceptionsText: TextView
    private lateinit var savingsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            prefs = getSharedPreferences("speedbump_prefs", Context.MODE_PRIVATE)

            val mainLayout = ScrollView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#121212"))
                isFillViewport = true
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 80, 60, 80)
                gravity = Gravity.TOP
            }

            // --- Header ---
            val header = TextView(this).apply {
                text = "Financial Health"
                setTextColor(Color.WHITE)
                textSize = 32f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 80)
            }
            container.addView(header)

            // --- Stats Dashboard ---
            val statsCard = createCardLayout()
            
            val statsTitle = TextView(this).apply {
                text = "Your Impact"
                setTextColor(Color.LTGRAY)
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 20)
            }
            statsCard.addView(statsTitle)

            val statsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            interceptionsText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                text = "${prefs.getInt("interceptions", 0)}\nStops"
                setTextColor(Color.WHITE)
                textSize = 20f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }
            
            savingsText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                text = "$${prefs.getFloat("savings", 0f).toInt()}\nSaved"
                setTextColor(Color.parseColor("#4CAF50"))
                textSize = 20f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }

            statsRow.addView(interceptionsText)
            statsRow.addView(savingsText)
            statsCard.addView(statsRow)
            container.addView(statsCard)

            // --- Settings Section ---
            addSectionHeader(container, "Configuration")

            val wageGroup = createInputField("Hourly Wage ($)", "25.0", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            val wageInput = wageGroup.findViewById<EditText>(1001)
            wageInput.setText(prefs.getFloat("hourly_wage", 25f).toString())
            container.addView(wageGroup)

            val goalGroup = createInputField("Savings Goal", "Emergency Fund", InputType.TYPE_CLASS_TEXT)
            val goalInput = goalGroup.findViewById<EditText>(1001)
            goalInput.setText(prefs.getString("savings_goal", "Financial Freedom"))
            container.addView(goalGroup)

            val saveButton = createStyledButton("Update Configuration", "#6200EE") {
                try {
                    val wage = wageInput.text.toString().toFloatOrNull() ?: 25f
                    val goal = goalInput.text.toString()
                    prefs.edit().putFloat("hourly_wage", wage).putString("savings_goal", goal).apply()
                    Toast.makeText(this@MainActivity, "Configuration Saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("Speedbump", "Save Error: ${e.message}")
                }
            }
            container.addView(saveButton)

            // --- Permissions Section ---
            addSectionHeader(container, "System Access")

            container.addView(createStyledButton("Enable Overlay Permission", "#333333") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            })

            container.addView(createStyledButton("Enable Usage Access", "#333333") {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            })

            mainLayout.addView(container)
            setContentView(mainLayout)

            startMonitorService()
            
            // Check for updates
            UpdateManager(this).checkForUpdates()

        } catch (e: Exception) {
            android.util.Log.e("Speedbump", "Startup Crash: ${e.message}")
            Toast.makeText(this, "Crash: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        interceptionsText.text = "${prefs.getInt("interceptions", 0)}\nStops"
        savingsText.text = "$${prefs.getFloat("savings", 0f).toInt()}\nSaved"
    }

    private fun createCardLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E1E"))
                cornerRadius = 24f
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 60) }
        }
    }

    private fun addSectionHeader(container: LinearLayout, title: String) {
        val header = TextView(this).apply {
            text = title.uppercase()
            setTextColor(Color.GRAY)
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 20, 0, 20)
        }
        container.addView(header)
    }

    private fun createInputField(label: String, hintText: String, inputType: Int): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 30)
        }
        
        val labelView = TextView(this).apply {
            text = label
            setTextColor(Color.LTGRAY)
            textSize = 14f
            setPadding(0, 0, 0, 10)
        }
        
        val editText = EditText(this).apply {
            id = 1001
            hint = hintText
            setHintTextColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            this.inputType = inputType
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#252525"))
                cornerRadius = 12f
            }
            background = bg
            setPadding(30, 30, 30, 30)
        }
        
        layout.addView(labelView)
        layout.addView(editText)
        return layout
    }

    private fun createStyledButton(text: String, colorHex: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor(colorHex))
                cornerRadius = 12f
            }
            background = bg
            setAllCaps(false)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
            setOnClickListener { onClick() }
        }
    }

    private fun startMonitorService() {
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
    }
}
