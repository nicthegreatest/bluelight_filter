package com.example.bluelight_filter

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class AccessibilityFilterService : AccessibilityService() {

    private lateinit var overlayView: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayView = View(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, layoutParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intensity = intent?.getIntExtra("intensity", 0) ?: 0
        val dimming = intent?.getIntExtra("dimming", 0) ?: 0

        // Red Hue Logic
        val redHueAlpha = (intensity / 100.0f * 100).toInt().coerceIn(0, 255)
        val red = 255
        val green = 255 - (intensity * 1.5).toInt().coerceIn(0, 255)
        val blue = 255 - (intensity * 2.0).toInt().coerceIn(0, 255)
        val color = Color.argb(redHueAlpha, red, green, blue)
        overlayView.setBackgroundColor(color)

        // Dimming Logic
        if (dimming == 0) {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        } else {
            val brightness = 0.8f - (((dimming - 1) / 99.0f) * 0.8f)
            layoutParams.screenBrightness = brightness.coerceIn(0.0f, 1.0f)
        }

        windowManager.updateViewLayout(overlayView, layoutParams)

        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            windowManager.updateViewLayout(overlayView, layoutParams)
            windowManager.removeView(overlayView)
        }
    }
}