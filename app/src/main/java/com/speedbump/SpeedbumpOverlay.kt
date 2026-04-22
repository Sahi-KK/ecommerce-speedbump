package com.speedbump

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SpeedbumpOverlay(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var timer: CountDownTimer? = null
    private var breatheAnimator: ObjectAnimator? = null

    fun show(hoursLost: Double) {
        if (overlayView != null) return

        val prefs = context.getSharedPreferences("speedbump_prefs", Context.MODE_PRIVATE)
        val savingsGoal = prefs.getString("savings_goal", "Financial Freedom")

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }

        val rootLayout = object : LinearLayout(context) {
            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                if (event?.keyCode == KeyEvent.KEYCODE_BACK) return true
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            orientation = LinearLayout.VERTICAL
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#000000"), Color.parseColor("#1A1A1A"))
            )
            background = gradient
            gravity = Gravity.CENTER
            setPadding(80, 80, 80, 80)
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val goalLabel = TextView(context).apply {
            text = "REMEMBER YOUR GOAL"
            setTextColor(Color.parseColor("#666666"))
            textSize = 12f
            letterSpacing = 0.2f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val goalText = TextView(context).apply {
            text = savingsGoal
            setTextColor(Color.WHITE)
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 60)
        }

        val costText = TextView(context).apply {
            text = "This purchase represents\n%.1f hours of your life.".format(hoursLost)
            setTextColor(Color.parseColor("#E0E0E0"))
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 100)
        }

        val timerText = TextView(context).apply {
            text = "01:00"
            setTextColor(Color.WHITE)
            textSize = 72f
            setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        val promptText = TextView(context).apply {
            text = "Breathe In..."
            setTextColor(Color.parseColor("#888888"))
            textSize = 20f
            setTypeface(null, Typeface.ITALIC)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 100)
        }

        // Dramatic Breathe Animation
        breatheAnimator = ObjectAnimator.ofPropertyValuesHolder(
            timerText,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.25f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.25f)
        ).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                if (fraction > 0.5f) {
                    promptText.text = "Breathe Out..."
                    promptText.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    promptText.text = "Breathe In..."
                    promptText.setTextColor(Color.parseColor("#81C784"))
                }
            }
            start()
        }

        val exitButton = Button(context).apply {
            text = "I've reconsidered"
            setTextColor(Color.WHITE)
            visibility = View.GONE
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#333333"))
                cornerRadius = 20f
            }
            background = bg
            setPadding(80, 40, 80, 40)
            setOnClickListener { hide() }
        }

        rootLayout.addView(goalLabel)
        rootLayout.addView(goalText)
        rootLayout.addView(costText)
        rootLayout.addView(timerText)
        rootLayout.addView(promptText)
        rootLayout.addView(exitButton)

        windowManager?.addView(rootLayout, layoutParams)
        overlayView = rootLayout

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                timerText.text = "%02d:%02d".format(minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "00:00"
                timerText.setTextColor(Color.parseColor("#4CAF50"))
                breatheAnimator?.cancel()
                showSurvey(rootLayout, exitButton)
            }
        }.start()
    }

    private fun showSurvey(rootLayout: LinearLayout, exitButton: Button) {
        val surveyContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 0)
        }

        val questionText = TextView(context).apply {
            text = "Why were you opening this app?"
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        surveyContainer.addView(questionText)

        val reasons = listOf("Boredom", "Stress", "Serious Matter", "Impulse")
        reasons.forEach { reason ->
            val btn = Button(context).apply {
                text = reason
                setTextColor(Color.LTGRAY)
                setBackgroundColor(Color.TRANSPARENT)
                setAllCaps(false)
                setOnClickListener {
                    saveReason(reason)
                    surveyContainer.visibility = View.GONE
                    exitButton.visibility = View.VISIBLE
                }
            }
            surveyContainer.addView(btn)
        }

        exitButton.setOnClickListener {
            val prefs = context.getSharedPreferences("speedbump_prefs", Context.MODE_PRIVATE)
            val lastReason = prefs.getString("last_reason", "")
            
            if (lastReason != "Serious Matter") {
                // Successful Stop!
                val interceptions = prefs.getInt("interceptions", 0) + 1
                val savings = prefs.getFloat("savings", 0f) + 50f
                prefs.edit()
                    .putInt("interceptions", interceptions)
                    .putFloat("savings", savings)
                    .apply()
                
                // Take them home to help them stop
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(homeIntent)
            }
            hide()
        }

        rootLayout.addView(surveyContainer)
    }

    private fun saveReason(reason: String) {
        val prefs = context.getSharedPreferences("speedbump_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("reason_$reason", 0)
        prefs.edit()
            .putInt("reason_$reason", currentCount + 1)
            .putString("last_reason", reason)
            .apply()
    }

    private fun hide() {
        timer?.cancel()
        breatheAnimator?.cancel()
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
}
