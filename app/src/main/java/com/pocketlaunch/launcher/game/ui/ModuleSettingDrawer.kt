package com.pocketlaunch.launcher.game.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
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
        // Note: Make sure InkTheme is imported if you use it, or fallback to standard colors
        setBackgroundColor(Color.parseColor("#151824"))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 8, 0, 16)
        }
        visibility = GONE
    }

    fun populateSettings(module: Module) {
        removeAllViews()

        val header = TextView(context).apply {
            text = "${module.name} Options"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        }
        addView(header)

        addSliderOption("HUD Scale", 100, 200) { scaleVal -> }
        addSliderOption("Background Opacity", 80, 100) { opacityVal -> }
    }

    private fun addSliderOption(label: String, defaultVal: Int, maxVal: Int, onChange: (Int) -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL // <-- This is what was crashing! Now fixed by the import at the top.
            setPadding(0, 8, 0, 8)
        }

        val labelTv = TextView(context).apply {
            text = "$label: $defaultVal"
            setTextColor(Color.parseColor("#94A3B8"))
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
