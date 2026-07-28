package com.pocketlaunch.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

/**
 * ClientHudDrawer
 * Generates an authentic, clean Minecraft Client menu layout (similar to Lunar/Feather).
 * Focused on readability, low memory footprint, and true functionality.
 */
class ClientHudDrawer(
    private val context: Context,
    private val configManager: ClientConfigManager
) {

    // Main Container View
    val mainLayout: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            (280 * context.resources.displayMetrics.density).toInt(), // Fixed 280dp client panel width
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setPadding(24, 32, 24, 32)

        // Authentic Dark Semi-Transparent Panel Background
        val darkPanelBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#E60F0F14")) // Deep obsidian charcoal with 90% opacity
            cornerRadius = 16f
            setStroke(2, Color.parseColor("#2A2A36")) // Subtle clean border
        }
        background = darkPanelBackground
    }

    private val contentContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    init {
        buildHeader()
        buildModList()
    }

    /**
     * Builds the Top Title Bar with Client Name & Subtitle
     */
    private fun buildHeader() {
        val titleText = TextView(context).apply {
            text = "INK CLIENT"
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        val subtitleText = TextView(context).apply {
            text = "v1.8.9 • Performance & Overlay Engine"
            textSize = 11f
            setTextColor(Color.parseColor("#8E8E9B"))
            setPadding(0, 4, 0, 24)
        }

        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            setBackgroundColor(Color.parseColor("#2A2A36"))
        }

        mainLayout.addView(titleText)
        mainLayout.addView(subtitleText)
        mainLayout.addView(divider)

        // Scrollable area for mods and options
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            isVerticalScrollBarEnabled = false
        }

        scrollView.addView(contentContainer)
        mainLayout.addView(scrollView)
    }

    /**
     * Populates real, working client mods and game performance toggles
     */
    private fun buildModList() {
        contentContainer.removeAllViews()

        // Category 1: GAMEPLAY MODS
        addCategoryHeader("GAMEPLAY & VISUALS")

        addToggleOption(
            title = "Custom Touch Controls",
            description = "Overlay dynamic screen buttons for PC controls",
            initialValue = configManager.isCustomControlsEnabled
        ) { isChecked ->
            configManager.isCustomControlsEnabled = isChecked
            configManager.saveClientProperties()
        }

        addSliderOption(
            title = "Field of View (FOV)",
            min = 30,
            max = 110,
            currentValue = configManager.currentFovSetting.toInt(),
            unit = "°"
        ) { newValue ->
            configManager.setFov(newValue.toFloat())
        }

        addSliderOption(
            title = "Framerate Limit",
            min = 30,
            max = 240,
            currentValue = configManager.maxFpsSetting,
            unit = " FPS"
        ) { newValue ->
            configManager.maxFpsSetting = newValue
            configManager.syncWithGameOptions()
        }

        // Category 2: ENGINE & PERFORMANCE
        addCategoryHeader("PERFORMANCE ENHANCEMENTS")

        addToggleOption(
            title = "FPS Boost Mode",
            description = "Lowers render distance & smooth lighting for performance",
            initialValue = configManager.isPerformanceBoostEnabled
        ) { isChecked ->
            configManager.setPerformanceMode(isChecked)
        }

        addToggleOption(
            title = "Glassmorphism UI",
            description = "Applies frosted dark styling to client overlays",
            initialValue = configManager.isGlassUiEnabled
        ) { isChecked ->
            configManager.isGlassUiEnabled = isChecked
            configManager.saveClientProperties()
        }
    }

    /**
     * Helper to render category subheaders
     */
    private fun addCategoryHeader(title: String) {
        val header = TextView(context).apply {
            text = title
            textSize = 11f
            setTextColor(Color.parseColor("#525266"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 16, 0, 12)
        }
        contentContainer.addView(header)
    }

    /**
     * Helper to create clean toggle rows
     */
    private fun addToggleOption(
        title: String,
        description: String,
        initialValue: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        }

        val titleView = TextView(context).apply {
            text = title
            textSize = 14f
            setTextColor(Color.parseColor("#E1E1E6"))
        }

        val descView = TextView(context).apply {
            text = description
            textSize = 10f
            setTextColor(Color.parseColor("#727280"))
        }

        textContainer.addView(titleView)
        textContainer.addView(descView)

        val checkBox = CheckBox(context).apply {
            isChecked = initialValue
            setOnCheckedChangeListener { _, isChecked ->
                onChanged(isChecked)
            }
        }

        rowLayout.addView(textContainer)
        rowLayout.addView(checkBox)

        contentContainer.addView(rowLayout)
    }

    /**
     * Helper to create real, working slider settings
     */
    private fun addSliderOption(
        title: String,
        min: Int,
        max: Int,
        currentValue: Int,
        unit: String,
        onChanged: (Int) -> Unit
    ) {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val labelRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleView = TextView(context).apply {
            text = title
            textSize = 14f
            setTextColor(Color.parseColor("#E1E1E6"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        }

        val valueView = TextView(context).apply {
            text = "$currentValue$unit"
            textSize = 13f
            setTextColor(Color.parseColor("#55FF55")) // Authentic Minecraft Green accent
            typeface = Typeface.DEFAULT_BOLD
        }

        labelRow.addView(titleView)
        labelRow.addView(valueView)

        val seekBar = SeekBar(context).apply {
            this.max = max - min
            progress = currentValue - min
            setPadding(0, 16, 0, 0)

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val calculatedValue = progress + min
                    valueView.text = "$calculatedValue$unit"
                    if (fromUser) {
                        onChanged(calculatedValue)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        container.addView(labelRow)
        container.addView(seekBar)

        contentContainer.addView(container)
    }
}
