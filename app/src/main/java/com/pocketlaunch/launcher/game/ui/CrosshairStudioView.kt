package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

/**
 * Complete Crosshair Customization Studio Panel.
 * Includes real-time preview canvas, dynamic style selector, parameter sliders,
 * RGB rainbow toggles, hit-marker test button, and one-tap preset loader.
 */
class CrosshairStudioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val renderer: AdvancedCrosshairRenderer
    private val controlsContainer: LinearLayout
    private val styleButtonsList = mutableListOf<TextView>()

    init {
        orientation = VERTICAL
        setPadding(32, 28, 32, 28)
        background = InkTheme.createCardBackground(
            bgColor = InkTheme.bgPanel,
            borderColor = InkTheme.borderDark,
            borderWidthPx = 2,
            cornerRadiusPx = 28f
        )
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        // --- Studio Header ---
        val header = TextView(context).apply {
            text = "CROSSHAIR STUDIO PRO"
            setTextColor(InkTheme.textPrimary)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 20)
        }
        addView(header)

        // --- Real-time Interactive Preview Window ---
        val previewCard = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 30)
            background = InkTheme.createCardBackground(
                bgColor = InkTheme.bgVoid,
                borderColor = InkTheme.accentPrimary,
                borderWidthPx = 2,
                cornerRadiusPx = 20f
            )
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 260).apply {
                setMargins(0, 0, 0, 20)
            }
        }

        renderer = AdvancedCrosshairRenderer(context).apply {
            layoutParams = LayoutParams(200, 200)
        }
        previewCard.addView(renderer)
        addView(previewCard)

        // --- Action Row (Hit-Marker Test) ---
        val actionRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val testHitBtn = TextView(context).apply {
            text = "TEST HITMARKER"
            setTextColor(InkTheme.textPrimary)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(24, 14, 24, 14)
            background = InkTheme.createCardBackground(
                bgColor = InkTheme.accentDanger,
                borderColor = InkTheme.borderDark,
                borderWidthPx = 1,
                cornerRadiusPx = 12f
            )
            setOnClickListener { renderer.triggerHitMarker() }
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
        }

        actionRow.addView(testHitBtn)
        addView(actionRow)

        // --- Scrollable Controls Drawer ---
        val scrollView = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 500)
            isVerticalScrollBarEnabled = false
        }

        controlsContainer = LinearLayout(context).apply {
            orientation = VERTICAL
        }
        scrollView.addView(controlsContainer)
        addView(scrollView)

        buildControlPanel()
    }

    private fun buildControlPanel() {
        controlsContainer.removeAllViews()

        // 1. Crosshair Style Selector Bar
        addSectionHeader("CROSSHAIR STYLE")
        val styleBar = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, 0, 0, 20)
        }
        val styleContainer = LinearLayout(context).apply { orientation = HORIZONTAL }

        CrosshairStyle.values().forEach { styleOption ->
            val styleBtn = TextView(context).apply {
                text = styleOption.name.replace("_", " ")
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (styleOption == renderer.config.style) InkTheme.textPrimary else InkTheme.textMuted)
                setPadding(20, 12, 20, 12)
                background = createOptionTabBg(styleOption == renderer.config.style)
                setOnClickListener {
                    renderer.config.style = styleOption
                    updateStyleTabSelection(styleOption)
                    renderer.invalidate()
                }
            }
            styleButtonsList.add(styleBtn)
            styleContainer.addView(styleBtn)
        }
        styleBar.addView(styleContainer)
        controlsContainer.addView(styleBar)

        // 2. Preset Quick-Loaders
        addSectionHeader("PRESETS")
        val presetRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(0, 0, 0, 20)
        }
        addPresetButton(presetRow, "FLARIAL PRO") {
            renderer.config.style = CrosshairStyle.FLARIAL_PRO
            renderer.config.length = 14f
            renderer.config.gap = 5f
            renderer.config.thickness = 3f
            renderer.config.showDot = true
            renderer.config.primaryColor = Color.parseColor("#7C3AED")
            renderer.config.isRainbow = false
            refreshControlsAndRenderer()
        }
        addPresetButton(presetRow, "CS PRECISION") {
            renderer.config.style = CrosshairStyle.CROSS
            renderer.config.length = 10f
            renderer.config.gap = 3f
            renderer.config.thickness = 2f
            renderer.config.showDot = false
            renderer.config.showOutline = true
            renderer.config.primaryColor = Color.GREEN
            renderer.config.isRainbow = false
            refreshControlsAndRenderer()
        }
        addPresetButton(presetRow, "RAINBOW CIRCLE") {
            renderer.config.style = CrosshairStyle.CIRCLE
            renderer.config.gap = 12f
            renderer.config.thickness = 3.5f
            renderer.config.showDot = true
            renderer.config.isRainbow = true
            refreshControlsAndRenderer()
        }
        controlsContainer.addView(presetRow)

        // 3. Size & Dimension Sliders
        addSectionHeader("GEOMETRY & SIZING")
        addSlider("Length", renderer.config.length.toInt(), 2, 40) { valVal ->
            renderer.config.length = valVal.toFloat()
            renderer.invalidate()
        }
        addSlider("Gap Spacing", renderer.config.gap.toInt(), 0, 30) { valVal ->
            renderer.config.gap = valVal.toFloat()
            renderer.invalidate()
        }
        addSlider("Stroke Thickness", renderer.config.thickness.toInt(), 1, 12) { valVal ->
            renderer.config.thickness = valVal.toFloat()
            renderer.invalidate()
        }
        addSlider("Rotation Angle", renderer.config.rotationAngle.toInt(), 0, 180) { valVal ->
            renderer.config.rotationAngle = valVal.toFloat()
            renderer.invalidate()
        }

        // 4. Center Dot Controls
        addSectionHeader("CENTER DOT")
        addSwitchRow("Show Center Dot", renderer.config.showDot) { isChecked ->
            renderer.config.showDot = isChecked
            renderer.invalidate()
        }
        addSlider("Dot Radius", renderer.config.dotSize.toInt(), 1, 10) { valVal ->
            renderer.config.dotSize = valVal.toFloat()
            renderer.invalidate()
        }

        // 5. Outlines & Shadows
        addSectionHeader("OUTLINES & SHADOWS")
        addSwitchRow("Show Black Outline", renderer.config.showOutline) { isChecked ->
            renderer.config.showOutline = isChecked
            renderer.invalidate()
        }
        addSlider("Outline Width", renderer.config.outlineThickness.toInt(), 1, 6) { valVal ->
            renderer.config.outlineThickness = valVal.toFloat()
            renderer.invalidate()
        }

        // 6. Colors & Dynamic RGB
        addSectionHeader("COLOR ENGINE & RGB")
        addSwitchRow("Animated Rainbow RGB", renderer.config.isRainbow) { isChecked ->
            renderer.config.isRainbow = isChecked
            renderer.invalidate()
        }
        addSlider("Rainbow Speed", renderer.config.rainbowSpeed.toInt(), 1, 10) { valVal ->
            renderer.config.rainbowSpeed = valVal.toFloat()
            renderer.invalidate()
        }

        // 7. Dynamic Spread & Hit Markers
        addSectionHeader("COMBAT REACTIVITY")
        addSwitchRow("Dynamic Movement Spread", renderer.config.dynamicSpread) { isChecked ->
            renderer.config.dynamicSpread = isChecked
            renderer.invalidate()
        }
        addSwitchRow("Enable Hit-Markers", renderer.config.enableHitMarker) { isChecked ->
            renderer.config.enableHitMarker = isChecked
            renderer.invalidate()
        }
    }

    private fun addSectionHeader(title: String) {
        val tv = TextView(context).apply {
            text = title
            setTextColor(InkTheme.accentSecondary)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 12, 0, 8)
        }
        controlsContainer.addView(tv)
    }

    private fun addSlider(label: String, initialVal: Int, minVal: Int, maxVal: Int, onChange: (Int) -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 6, 0, 6)
        }

        val labelTv = TextView(context).apply {
            text = "$label: $initialVal"
            setTextColor(InkTheme.textSecondary)
            textSize = 11f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val seekBar = SeekBar(context).apply {
            max = maxVal - minVal
            progress = initialVal - minVal
            layoutParams = LayoutParams(240, LayoutParams.WRAP_CONTENT)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val actualVal = progress + minVal
                    labelTv.text = "$label: $actualVal"
                    onChange(actualVal)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        row.addView(labelTv)
        row.addView(seekBar)
        controlsContainer.addView(row)
    }

    private fun addSwitchRow(label: String, defaultChecked: Boolean, onToggle: (Boolean) -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 6, 0, 6)
        }

        val labelTv = TextView(context).apply {
            text = label
            setTextColor(InkTheme.textSecondary)
            textSize = 11f
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val switchView = Switch(context).apply {
            isChecked = defaultChecked
            setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
        }

        row.addView(labelTv)
        row.addView(switchView)
        controlsContainer.addView(row)
    }

    private fun addPresetButton(container: LinearLayout, name: String, onClick: () -> Unit) {
        val btn = TextView(context).apply {
            text = name
            setTextColor(InkTheme.textPrimary)
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(16, 10, 16, 10)
            background = InkTheme.createCardBackground(
                bgColor = InkTheme.bgCard,
                borderColor = InkTheme.borderDark,
                borderWidthPx = 1,
                cornerRadiusPx = 10f
            )
            setOnClickListener { onClick() }
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
        }
        container.addView(btn)
    }

    private fun updateStyleTabSelection(selectedStyle: CrosshairStyle) {
        for ((idx, btn) in styleButtonsList.withIndex()) {
            val styleOption = CrosshairStyle.values()[idx]
            val isSelected = styleOption == selectedStyle
            btn.setTextColor(if (isSelected) InkTheme.textPrimary else InkTheme.textMuted)
            btn.background = createOptionTabBg(isSelected)
        }
    }

    private fun createOptionTabBg(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(if (selected) InkTheme.bgCard else InkTheme.bgVoid)
            if (selected) setStroke(2, InkTheme.accentPrimary)
            cornerRadius = 10f
        }
    }

    private fun refreshControlsAndRenderer() {
        renderer.invalidate()
        buildControlPanel()
    }
}
