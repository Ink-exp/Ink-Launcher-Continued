package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.pocketlaunch.launcher.game.ModuleManager
import com.pocketlaunch.launcher.game.module.render.CpsFpsHudModule
import com.pocketlaunch.launcher.game.module.render.F3DebugModule
import com.pocketlaunch.launcher.game.module.render.InventoryHudModule

/**
 * Screen Overlay Canvas handling live widget rendering and drag-and-drop customization.
 */
class InkHudCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isEditMode: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val layoutManager = HudLayoutManager(context)
    private val registeredElements = mutableListOf<HudElement>()
    private var draggedElement: HudElement? = null

    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    // Editor bounding box paints
    private val outlinePaint = Paint().apply {
        color = InkTheme.accentPrimary
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val activePaint = Paint().apply {
        color = InkTheme.accentSuccess
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    init {
        setupDefaultElements()
    }

    private fun setupDefaultElements() {
        // 1. FPS / CPS Counter Widget
        val fpsCpsWidget = object : HudElement("cps_fps", "FPS/CPS Counter", 0.03f, 0.05f) {
            override fun render(canvas: Canvas, currentX: Float, currentY: Float) {
                (ModuleManager.getModule("cps_fps_hud") as? CpsFpsHudModule)?.let { module ->
                    if (module.isEnabled || isEditMode) {
                        module.renderHud(canvas, 60)
                    }
                }
            }
        }

        // 2. F3 Debug Info Widget
        val f3Widget = object : HudElement("f3_debug", "F3 Debug Info", 0.03f, 0.15f) {
            override fun render(canvas: Canvas, currentX: Float, currentY: Float) {
                (ModuleManager.getModule("f3_debug") as? F3DebugModule)?.let { module ->
                    if (module.isEnabled || isEditMode) {
                        module.renderDebugInfo(canvas, 60, 0.0, 64.0, 0.0)
                    }
                }
            }
        }

        // 3. Armor / Inventory HUD Widget
        val armorWidget = object : HudElement("inventory_hud", "Armor Status", 0.75f, 0.82f) {
            override fun render(canvas: Canvas, currentX: Float, currentY: Float) {
                (ModuleManager.getModule("inventory_hud") as? InventoryHudModule)?.let { module ->
                    if (module.isEnabled || isEditMode) {
                        module.renderHud(canvas, width, height)
                    }
                }
            }
        }

        registeredElements.add(fpsCpsWidget)
        registeredElements.add(f3Widget)
        registeredElements.add(armorWidget)

        // Restore saved positions from preferences
        registeredElements.forEach { layoutManager.loadElementPosition(it) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (element in registeredElements) {
            val posX = element.xRatio * width
            val posY = element.yRatio * height

            element.render(canvas, posX, posY)

            // Draw edit boundary outline during HUD editing mode
            if (isEditMode) {
                val paint = if (element.isSelected) activePaint else outlinePaint
                canvas.drawRect(
                    posX - 8f,
                    posY - 8f,
                    posX + (element.widthPx * element.scale) + 8f,
                    posY + (element.heightPx * element.scale) + 8f,
                    paint
                )
            }
        }

        if (!isEditMode) {
            invalidate() // Continuous render loop for game overlays
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditMode) return false

        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggedElement = registeredElements.lastOrNull { it.containsPoint(touchX, touchY, width, height) }
                draggedElement?.let { element ->
                    registeredElements.forEach { it.isSelected = false }
                    element.isSelected = true
                    touchOffsetX = touchX - (element.xRatio * width)
                    touchOffsetY = touchY - (element.yRatio * height)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                draggedElement?.let { element ->
                    element.xRatio = ((touchX - touchOffsetX) / width).coerceIn(0f, 0.9f)
                    element.yRatio = ((touchY - touchOffsetY) / height).coerceIn(0f, 0.9f)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                draggedElement?.let { element ->
                    layoutManager.saveElementPosition(element)
                    draggedElement = null
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
