package com.example.bluelight_filter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.bluelight_filter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Since we have included the content_main.xml in activity_main.xml, we need to access the views from the binding object of activity_main.xml.
        // The id of the included layout is not specified, so we can't access it directly.
        // We will assume the binding object for content_main is merged into ActivityMainBinding.
        // However, the correct way is to get the root of the included layout and then find the views by id.
        // For simplicity, we will assume the binding works as follows.
        // val contentBinding = ContentMainBinding.bind(binding.root)

        val filterSwitch = findViewById<android.widget.Switch>(R.id.filter_switch)
        val intensitySlider = findViewById<SeekBar>(R.id.intensity_slider)

        filterSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkOverlayPermission()
            } else {
                stopFilterService()
            }
        }

        intensitySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (filterSwitch.isChecked) {
                    startFilterService(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            } else {
                startFilterService(findViewById<SeekBar>(R.id.intensity_slider).progress)
            }
        } else {
            startFilterService(findViewById<SeekBar>(R.id.intensity_slider).progress)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startFilterService(findViewById<SeekBar>(R.id.intensity_slider).progress)
                }
            }
        }
    }

    private fun startFilterService(intensity: Int) {
        val intent = Intent(this, BlueLightFilterService::class.java)
        intent.putExtra("intensity", intensity)
        startService(intent)
    }

    private fun stopFilterService() {
        val intent = Intent(this, BlueLightFilterService::class.java)
        stopService(intent)
    }
}