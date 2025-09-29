package com.example.bluelight_filter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.bluelight_filter.databinding.ActivityMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentIntensity = 0
    private var currentDimming = 0
    private lateinit var dimmingButtons: List<Button>

    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        if (Settings.canDrawOverlays(this)) {
            startFilterService(currentIntensity, currentDimming)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filterSwitch = findViewById<SwitchMaterial>(R.id.filter_switch)
        val intensitySlider = findViewById<SeekBar>(R.id.intensity_slider)

        dimmingButtons = listOf(
            findViewById(R.id.dim_20),
            findViewById(R.id.dim_40),
            findViewById(R.id.dim_60),
            findViewById(R.id.dim_80),
            findViewById(R.id.dim_90)
        )

        filterSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkOverlayPermission()
            } else {
                stopFilterService()
            }
        }

        intensitySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentIntensity = progress
                if (filterSwitch.isChecked) {
                    startFilterService(currentIntensity, currentDimming)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dimmingClickListener = View.OnClickListener { view ->
            val clickedButton = view as Button
            val isAlreadySelected = clickedButton.isSelected

            // Deselect all buttons first
            dimmingButtons.forEach { it.isSelected = false }

            if (isAlreadySelected) {
                // If the clicked button was already selected, turn off dimming
                currentDimming = 0
            } else {
                // Otherwise, select the new button and set the dimming level
                clickedButton.isSelected = true
                currentDimming = when (clickedButton.id) {
                    R.id.dim_20 -> 20
                    R.id.dim_40 -> 40
                    R.id.dim_60 -> 60
                    R.id.dim_80 -> 80
                    R.id.dim_90 -> 90
                    else -> 0
                }
            }

            if (filterSwitch.isChecked) {
                startFilterService(currentIntensity, currentDimming)
            }
        }

        dimmingButtons.forEach { it.setOnClickListener(dimmingClickListener) }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            requestOverlayPermissionLauncher.launch(intent)
        } else {
            startFilterService(currentIntensity, currentDimming)
        }
    }

    private fun startFilterService(intensity: Int, dimming: Int) {
        val intent = Intent(this, BlueLightFilterService::class.java)
        intent.putExtra("intensity", intensity)
        intent.putExtra("dimming", dimming)
        startService(intent)
    }

    private fun stopFilterService() {
        val intent = Intent(this, BlueLightFilterService::class.java)
        stopService(intent)
        // Deselect all buttons when the filter is turned off
        dimmingButtons.forEach { it.isSelected = false }
        currentDimming = 0
    }
}