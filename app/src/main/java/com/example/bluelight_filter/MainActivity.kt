package com.example.bluelight_filter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private var currentIntensity = 0
    private var currentDimming = 0
    private lateinit var dimmingButtons: List<Button>
    private lateinit var filterSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filterSwitch = findViewById(R.id.filter_switch)
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
                if (!isAccessibilityServiceEnabled()) {
                    promptToEnableAccessibilityService()
                    filterSwitch.isChecked = false // Reset switch, user must re-toggle after enabling
                } else {
                    startFilterService(currentIntensity, currentDimming)
                }
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

            dimmingButtons.forEach { it.isSelected = false }

            if (isAlreadySelected) {
                currentDimming = 0
            } else {
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

    override fun onResume() {
        super.onResume()
        // Sync the switch state with the actual service state when the user returns to the app
        filterSwitch.isChecked = isAccessibilityServiceEnabled()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${AccessibilityFilterService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(service, ignoreCase = false) == true
    }

    private fun promptToEnableAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun startFilterService(intensity: Int, dimming: Int) {
        val intent = Intent(this, AccessibilityFilterService::class.java)
        intent.putExtra("intensity", intensity)
        intent.putExtra("dimming", dimming)
        startService(intent)
    }

    private fun stopFilterService() {
        val intent = Intent(this, AccessibilityFilterService::class.java)
        stopService(intent)
        dimmingButtons.forEach { it.isSelected = false }
        currentDimming = 0
    }
}