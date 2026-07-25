package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.pocketlaunch.launcher.game.Module

/**
 * Expandable Sub-Settings Drawer attached under module cards.
 */
class ModuleSettingDrawer(context: Context) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setPadding(28, 20, 28, 20)
        background = InkTheme.createCardBackground(
            bgColor = InkTheme.bgPanel,
            borderColor = InkTheme.borderDark,
            borderWidthPx = 1,
            cornerRadiusPx = 16f
        )
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 8, 0, 16)
        }
        visibility = GONE
    }

    fun populateSettings(module: Module) {
        removeAllViews()

        val header = TextView(context).apply {
            text = "${module.name} Options"
            setTextColor(InkTheme.textPrimary)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        }
        addView(header)

        addSliderOption("HUD Scale", 100, 200) { scaleVal -> }
        addSliderOption("Background Opacity", 80, 100) { opacityVal -> }

        if (module.id == "attack_indicator" || module.id == "cps_fps_hud") {
            val colorPicker = ColorPickerView(context, "Accent / Text Color", Color.WHITE) { newColor ->
                // Color customization callback
            }
            addView(colorPicker)
        }
    }

    private fun addSliderOption(label: String, defaultVal: Int, maxVal: Int, onChange: (Int) -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val labelTv = TextView(context).apply {
            text = "$label: $defaultVal"
            setTextColor(InkTheme.textSecondary)
            textSize = 11f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val seekBar = SeekBar(context).apply {
            max = maxVal
            progress = defaultVal
            layoutParams = LayoutParams(240, LayoutParams.WRAP_CONTENT)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valVal: Int, fromUser: Boolean) {
                    labelTv.text = "$label: $valVal"
                    onChange(valVal)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        row.addView(labelTv)
        row.addView(seekBar)
        addView(row)
    }

    fun toggleExpansion() {
        visibility = if (visibility == VISIBLE) GONE else VISIBLE
    }
}
