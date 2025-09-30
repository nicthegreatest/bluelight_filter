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
        @Suppress("DEPRECATION")
        overlayView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        windowManager.addView(overlayView, layoutParams)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intensity = intent?.getIntExtra("intensity", 0) ?: 0
        val dimming = intent?.getIntExtra("dimming", 0) ?: 0

        // --- Combined Color Calculation ---

        // 1. Define the alpha (opacity) for the red hue and dimming effects.
        // Red hue alpha is scaled to a max of ~60% opacity to avoid oversaturation.
        val alphaRed = (intensity / 100.0f) * 0.6f
        val alphaDim = dimming / 100.0f

        // 2. Calculate the RGB components for the red hue filter.
        val red = 255
        val green = 255 - (intensity * 1.5).toInt().coerceIn(0, 255)
        val blue = 255 - (intensity * 2.0).toInt().coerceIn(0, 255)

        // 3. Blend the two layers (dimming black layer over red hue layer).
        // This formula calculates the final color of a single overlay that has both effects.
        val finalAlpha = alphaRed + alphaDim - (alphaRed * alphaDim)

        if (finalAlpha < 0.01f) {
            overlayView.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val finalRed = (red * alphaRed * (1 - alphaDim)) / finalAlpha
            val finalGreen = (green * alphaRed * (1 - alphaDim)) / finalAlpha
            val finalBlue = (blue * alphaRed * (1 - alphaDim)) / finalAlpha

            val finalColor = Color.argb(
                (finalAlpha * 255).toInt(),
                finalRed.toInt(),
                finalGreen.toInt(),
                finalBlue.toInt()
            )
            overlayView.setBackgroundColor(finalColor)
        }

        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}