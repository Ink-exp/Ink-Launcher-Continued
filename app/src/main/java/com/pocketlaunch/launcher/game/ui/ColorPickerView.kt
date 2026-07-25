package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/**
 * Modern RGB Color Picker control card for crosshairs, HUD elements, and waypoints.
 */
class ColorPickerView(
    context: Context,
    val title: String,
    initialColor: Int = Color.WHITE,
    val onColorChanged: (Int) -> Unit
) : LinearLayout(context) {

    private var red = Color.red(initialColor)
    private var green = Color.green(initialColor)
    private var blue = Color.blue(initialColor)

    private val colorPreview: TextView

    init {
        orientation = VERTICAL
        setPadding(24, 20, 24, 20)
        background = InkTheme.createCardBackground(
            bgColor = InkTheme.bgCard,
            borderColor = InkTheme.borderDark,
            borderWidthPx = 1,
            cornerRadiusPx = 16f
        )

        val headerRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }

        val titleTv = TextView(context).apply {
            text = title
            setTextColor(InkTheme.textPrimary)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        colorPreview = TextView(context).apply {
            layoutParams = LayoutParams(50, 50)
            background = GradientDrawable().apply {
                setColor(initialColor)
                cornerRadius = 10f
                setStroke(2, InkTheme.borderDark)
            }
        }

        headerRow.addView(titleTv)
        headerRow.addView(colorPreview)
        addView(headerRow)

        addChannelSlider("R", red, Color.RED) { red = it; updateColor() }
        addChannelSlider("G", green, Color.GREEN) { green = it; updateColor() }
        addChannelSlider("B", blue, Color.BLUE) { blue = it; updateColor() }
    }

    private fun addChannelSlider(label: String, initialVal: Int, tintColor: Int, onChange: (Int) -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4, 0, 4)
        }

        val labelTv = TextView(context).apply {
            text = label
            setTextColor(tintColor)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 16, 0)
        }

        val seekBar = SeekBar(context).apply {
            max = 255
            progress = initialVal
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    onChange(progress)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        row.addView(labelTv)
        row.addView(seekBar)
        addView(row)
    }

    private fun updateColor() {
        val currentColor = Color.rgb(red, green, blue)
        (colorPreview.background as? GradientDrawable)?.setColor(currentColor)
        onColorChanged(currentColor)
    }
}
