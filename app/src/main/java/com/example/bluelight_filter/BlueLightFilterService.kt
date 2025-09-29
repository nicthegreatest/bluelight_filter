package com.example.bluelight_filter

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class BlueLightFilterService : Service() {

    private lateinit var overlayView: View

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intensity = intent?.getIntExtra("intensity", 20) ?: 20
        val red = 255
        val green = 255 - (intensity * 2.55).toInt()
        val blue = 255 - (intensity * 2.55).toInt()
        val color = Color.argb(50, red, green, blue)

        if (!::overlayView.isInitialized) {
            overlayView = View(this)
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, layoutParams)
        }

        overlayView.setBackgroundColor(color)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
        }
    }
}