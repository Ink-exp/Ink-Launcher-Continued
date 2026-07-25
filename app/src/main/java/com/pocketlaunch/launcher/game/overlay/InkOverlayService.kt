package com.pocketlaunch.launcher.overlay

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.pocketlaunch.launcher.game.ModuleCategory
import com.pocketlaunch.launcher.game.ModuleManager
import com.pocketlaunch.launcher.ui.InkTheme
import com.pocketlaunch.launcher.ui.UiAnimationUtils
import kotlin.math.abs

class InkOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var rootOverlay: FrameLayout
    private lateinit var mainPanel: LinearLayout
    private lateinit var modulesContainer: LinearLayout
    private var isMenuOpen = false
    private var activeCategory = ModuleCategory.RENDER

    // Ink UI Design Tokens
    private val bgVoid = "#0A0B10"
    private val cardDark = "#131520"
    private val cardBorder = "#1E2235"
    private val accentPurple = "#7C3AED"
    private val textWhite = "#FFFFFF"
    private val textMuted = "#8A92B2"

    // Floating trigger geometry, in px. The glow rings + border are drawn inside the view
    // bounds, so the box is padded by that much to keep the glass face ~110px as before.
    private val triggerGlowSpreadPx = 8
    private val triggerBorderPx = 3
    private val triggerSizePx = 110 + 2 * (triggerGlowSpreadPx + triggerBorderPx)
    private val triggerBouncePx = 6

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        rootOverlay = FrameLayout(this).apply {
            // Let the trigger's release bounce overdraw its layout box instead of being clipped.
            clipChildren = false
            clipToPadding = false
        }

        // Floating Trigger Button - glassmorphism squircle with an accent glow border
        val triggerBtn = TextView(this).apply {
            text = "INK"
            setTextColor(InkTheme.textPrimary)
            textSize = 12f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.18f
            gravity = Gravity.CENTER
            background = InkTheme.createGlassGlowBackground(
                cornerRadiusPx = 34f,
                borderWidthPx = triggerBorderPx,
                glowSpreadPx = triggerGlowSpreadPx
            )
            layoutParams = FrameLayout.LayoutParams(triggerSizePx, triggerSizePx).apply {
                // Slack so the tap-release bounce is never clipped by the window edge.
                setMargins(triggerBouncePx, triggerBouncePx, triggerBouncePx, triggerBouncePx)
            }
        }

        // Overlay Main Card
        mainPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(bgVoid))
                setStroke(2, Color.parseColor(cardBorder))
                cornerRadius = 20f
            }
            layoutParams = FrameLayout.LayoutParams(640, 750).apply {
                setMargins(20, 130, 0, 0)
            }
        }

        // Header Title
        val header = TextView(this).apply {
            text = "INK CLIENT"
            setTextColor(Color.parseColor(textWhite))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 20)
        }
        mainPanel.addView(header)

        // Category Tab Bar
        val categoryBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 20)
        }

        ModuleCategory.values().forEach { category ->
            val tabBtn = TextView(this).apply {
                text = category.displayName
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (category == activeCategory) Color.parseColor(textWhite) else Color.parseColor(textMuted))
                setPadding(22, 12, 22, 12)
                background = createTabBackground(category == activeCategory)
                setOnClickListener {
                    activeCategory = category
                    refreshTabs(categoryBar)
                    renderCategoryModules()
                }
            }
            categoryBar.addView(tabBtn)
        }
        mainPanel.addView(categoryBar)

        // Modules Scroll Container
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isVerticalScrollBarEnabled = false
        }

        modulesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(modulesContainer)
        mainPanel.addView(scrollView)

        rootOverlay.addView(mainPanel)
        rootOverlay.addView(triggerBtn)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 120
        }

        // Touch Listener for Dragging & Opening
        triggerBtn.setOnTouchListener(object : View.OnTouchListener {
            private var initX = 0; private var initY = 0; private var touchX = 0f; private var touchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x; initY = params.y; touchX = event.rawX; touchY = event.rawY
                        UiAnimationUtils.pressTapScale(v)
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (event.rawX - touchX).toInt()
                        params.y = initY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(rootOverlay, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        UiAnimationUtils.releaseTapScale(v)
                        if (abs(event.rawX - touchX) < 12 && abs(event.rawY - touchY) < 12) {
                            isMenuOpen = !isMenuOpen
                            mainPanel.visibility = if (isMenuOpen) View.VISIBLE else View.GONE
                            if (isMenuOpen) renderCategoryModules()
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        // Without this the button stays shrunk if the gesture gets stolen.
                        UiAnimationUtils.releaseTapScale(v)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(rootOverlay, params)
    }

    private fun renderCategoryModules() {
        modulesContainer.removeAllViews()
        val categoryModules = ModuleManager.getModulesByCategory(activeCategory)

        if (categoryModules.isEmpty()) {
            modulesContainer.addView(TextView(this).apply {
                text = "No modules registered in this category."
                setTextColor(Color.parseColor(textMuted))
                textSize = 12f
                setPadding(0, 20, 0, 20)
            })
            return
        }

        for (module in categoryModules) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(28, 22, 28, 22)
                gravity = Gravity.CENTER_VERTICAL
                background = GradientDrawable().apply {
                    setColor(Color.parseColor(cardDark))
                    setStroke(1, Color.parseColor(cardBorder))
                    cornerRadius = 14f
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 14) }
            }

            val labelBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            labelBlock.addView(TextView(this).apply {
                text = module.name
                setTextColor(Color.parseColor(textWhite))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            })

            labelBlock.addView(TextView(this).apply {
                text = module.description
                setTextColor(Color.parseColor(textMuted))
                textSize = 10f
            })

            val switchWidget = Switch(this).apply {
                isChecked = module.isEnabled
                setOnCheckedChangeListener { _, _ ->
                    ModuleManager.toggleModule(module.id, this@InkOverlayService)
                }
            }

            card.addView(labelBlock)
            card.addView(switchWidget)
            modulesContainer.addView(card)
        }
    }

    private fun refreshTabs(categoryBar: LinearLayout) {
        for (i in 0 until categoryBar.childCount) {
            val tab = categoryBar.getChildAt(i) as TextView
            val isSelected = tab.text.toString().equals(activeCategory.displayName, ignoreCase = true)
            tab.setTextColor(if (isSelected) Color.parseColor(textWhite) else Color.parseColor(textMuted))
            tab.background = createTabBackground(isSelected)
        }
    }

    private fun createTabBackground(active: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(if (active) cardDark else bgVoid))
            if (active) setStroke(1, Color.parseColor(accentPurple))
            cornerRadius = 10f
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::rootOverlay.isInitialized) windowManager.removeView(rootOverlay)
    }
}
