package com.speedbump

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
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

    fun show(hoursLost: Double) {
        if (overlayView != null) return // Already showing

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

        // Create UI programmatically
        val rootLayout = object : LinearLayout(context) {
            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                // Intercept back button
                if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            orientation = VERTICAL
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val messageText = TextView(context).apply {
            text = "This costs %.1f hours of your life.".format(hoursLost)
            setTextColor(Color.WHITE)
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 50)
        }

        val timerText = TextView(context).apply {
            text = "01:00"
            setTextColor(Color.WHITE)
            textSize = 48f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        val exitButton = Button(context).apply {
            text = "Exit Overlay"
            visibility = View.GONE
            setOnClickListener { hide() }
        }

        rootLayout.addView(messageText)
        rootLayout.addView(timerText)
        rootLayout.addView(exitButton)

        windowManager?.addView(rootLayout, layoutParams)
        overlayView = rootLayout

        // Start 60-second timer
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timerText.text = "00:%02d".format(seconds)
            }

            override fun onFinish() {
                timerText.text = "00:00"
                exitButton.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun hide() {
        timer?.cancel()
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
}
