package com.pocketlaunch.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

/**
 * Self-Contained Client Engine & HUD Drawer for Ink Launcher
 * Combines configuration state management and authentic Minecraft client UI.
 */
class ClientHudDrawer(private val context: Context) {

    private val TAG = "InkClientEngine"
    private val clientDir: File = File(context.filesDir, "ink_launcher")
    private val optionsFile: File = File(clientDir, "game/options.txt")
    private val clientConfigFile: File = File(clientDir, "client_settings.properties")
    private val clientProperties = Properties()

    // Active Engine State
    var isGlassUiEnabled: Boolean = true
    var isCustomControlsEnabled: Boolean = true
    var isPerformanceBoostEnabled: Boolean = false
    var currentFovSetting: Float = 70.0f
    var maxFpsSetting: Int = 120

    // Main HUD Container View
    val mainLayout: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            (280 * context.resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setPadding(24, 32, 24, 32)

        // Authentic Obsidian Dark Card Styling
        val darkBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#E60F0F14"))
            cornerRadius = 16f
            setStroke(2, Color.parseColor("#2A2A36"))
        }
        background = darkBackground
    }

    private val contentContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    init {
        ensureDirectoryStructure()
        loadClientProperties()
        buildHeader()
        buildModList()
    }

    // --- CONFIG ENGINE LOGIC ---

    private fun ensureDirectoryStructure() {
        try {
            if (!clientDir.exists()) clientDir.mkdirs()
            val gameDir = File(clientDir, "game")
            if (!gameDir.exists()) gameDir.mkdirs()
            if (!optionsFile.exists()) {
                optionsFile.createNewFile()
                writeDefaultGameOptions()
            }
            if (!clientConfigFile.exists()) {
                clientConfigFile.createNewFile()
                saveDefaultClientProperties()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Directory init failed: ${e.localizedMessage}")
        }
    }

    private fun writeDefaultGameOptions() {
        try {
            FileWriter(optionsFile).use { writer ->
                writer.write("fov:0.0\nfpsLimit:120\nfancyGraphics:true\nao:2\nrenderDistance:8\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing options.txt: ${e.localizedMessage}")
        }
    }

    private fun saveDefaultClientProperties() {
        clientProperties.setProperty("ui.glassmorphism", "true")
        clientProperties.setProperty("ui.touch_controls", "true")
        clientProperties.setProperty("engine.performance_boost", "false")
        clientProperties.setProperty("game.fov", "70.0")
        clientProperties.setProperty("game.max_fps", "120")
        saveClientProperties()
    }

    fun loadClientProperties() {
        if (!clientConfigFile.exists()) return
        try {
            FileReader(clientConfigFile).use { reader -> clientProperties.load(reader) }
            isGlassUiEnabled = clientProperties.getProperty("ui.glassmorphism", "true").toBoolean()
            isCustomControlsEnabled = clientProperties.getProperty("ui.touch_controls", "true").toBoolean()
            isPerformanceBoostEnabled = clientProperties.getProperty("engine.performance_boost", "false").toBoolean()
            currentFovSetting = clientProperties.getProperty("game.fov", "70.0").toFloatOrNull() ?: 70.0f
            maxFpsSetting = clientProperties.getProperty("game.max_fps", "120").toIntOrNull() ?: 120
        } catch (e: Exception) {
            Log.e(TAG, "Error loading client state: ${e.localizedMessage}")
        }
    }

    fun saveClientProperties() {
        try {
            clientProperties.setProperty("ui.glassmorphism", isGlassUiEnabled.toString())
            clientProperties.setProperty("ui.touch_controls", isCustomControlsEnabled.toString())
            clientProperties.setProperty("engine.performance_boost", isPerformanceBoostEnabled.toString())
            clientProperties.setProperty("game.fov", currentFovSetting.toString())
            clientProperties.setProperty("game.max_fps", maxFpsSetting.toString())

            FileWriter(clientConfigFile).use { writer ->
                clientProperties.store(writer, "Ink Launcher Client Settings")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving client state: ${e.localizedMessage}")
        }
    }

    fun updateGameOption(key: String, value: String) {
        if (!optionsFile.exists()) ensureDirectoryStructure()
        try {
            val lines = if (optionsFile.exists()) optionsFile.readLines().toMutableList() else mutableListOf()
            var found = false
            for (i in lines.indices) {
                if (lines[i].startsWith("$key:")) {
                    lines[i] = "$key:$value"
                    found = true
                    break
                }
            }
            if (!found) lines.add("$key:$value")
            FileWriter(optionsFile).use { writer ->
                lines.forEach { line -> writer.write(line + "\n") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating options.txt: ${e.localizedMessage}")
        }
    }

    fun syncWithGameOptions() {
        val normalizedFov = ((currentFovSetting - 70.0f) / 40.0f).coerceIn(0.0f, 1.0f)
        updateGameOption("fov", normalizedFov.toString())
        updateGameOption("fpsLimit", maxFpsSetting.toString())

        if (isPerformanceBoostEnabled) {
            updateGameOption("fancyGraphics", "false")
            updateGameOption("ao", "0")
            updateGameOption("renderDistance", "6")
        } else {
            updateGameOption("fancyGraphics", "true")
            updateGameOption("ao", "2")
            updateGameOption("renderDistance", "10")
        }
    }

    // --- AUTHENTIC MINECRAFT UI DRAWING ---

    private fun buildHeader() {
        val titleText = TextView(context).apply {
            text = "INK CLIENT"
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        val subtitleText = TextView(context).apply {
            text = "v1.8.9 • Client & HUD Engine"
            textSize = 11f
            setTextColor(Color.parseColor("#8E8E9B"))
            setPadding(0, 4, 0, 20)
        }

        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 0, 0, 20)
            }
            setBackgroundColor(Color.parseColor("#2A2A36"))
        }

        mainLayout.addView(titleText)
        mainLayout.addView(subtitleText)
        mainLayout.addView(divider)

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

    private fun buildModList() {
        contentContainer.removeAllViews()

        addCategoryHeader("GAMEPLAY & OVERLAYS")

        addToggleOption(
            title = "Custom Touch Controls",
            description = "Overlay dynamic screen buttons for PC controls",
            initialValue = isCustomControlsEnabled
        ) { isChecked ->
            isCustomControlsEnabled = isChecked
            saveClientProperties()
        }

        addSliderOption(
            title = "Field of View (FOV)",
            min = 30,
            max = 110,
            currentValue = currentFovSetting.toInt(),
            unit = "°"
        ) { newValue ->
            currentFovSetting = newValue.toFloat()
            saveClientProperties()
            syncWithGameOptions()
        }

        addSliderOption(
            title = "Framerate Limit",
            min = 30,
            max = 240,
            currentValue = maxFpsSetting,
            unit = " FPS"
        ) { newValue ->
            maxFpsSetting = newValue
            saveClientProperties()
            syncWithGameOptions()
        }

        addCategoryHeader("PERFORMANCE & GRAPHICS")

        addToggleOption(
            title = "FPS Boost Mode",
            description = "Optimizes render distance & smooth lighting",
            initialValue = isPerformanceBoostEnabled
        ) { isChecked ->
            isPerformanceBoostEnabled = isChecked
            saveClientProperties()
            syncWithGameOptions()
        }

        addToggleOption(
            title = "Glassmorphism UI",
            description = "Applies translucent obsidian styling to HUD",
            initialValue = isGlassUiEnabled
        ) { isChecked ->
            isGlassUiEnabled = isChecked
            saveClientProperties()
        }
    }

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
            setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        }

        rowLayout.addView(textContainer)
        rowLayout.addView(checkBox)

        contentContainer.addView(rowLayout)
    }

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
            setPadding(0, 12, 0, 0)

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val calculatedValue = progress + min
                    valueView.text = "$calculatedValue$unit"
                    if (fromUser) onChanged(calculatedValue)
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
