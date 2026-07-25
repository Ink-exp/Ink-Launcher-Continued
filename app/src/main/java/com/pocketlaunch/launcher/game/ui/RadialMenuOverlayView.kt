package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.pocketlaunch.launcher.game.ModuleManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Touch-gesture Radial Wheel for quick toggles (Zoom, Perspective, Crosshair Studio, etc.).
 */
class RadialMenuOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class RadialItem(val label: String, val action: () -> Unit)

    private val items = mutableListOf<RadialItem>()
    private var hoveredIndex = -1
    private val outerRadius = 220f
    private val innerRadius = 80f

    private val arcPaint = Paint().apply { isAntiAlias = true }
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = InkTheme.borderDark
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    var onOpenCrosshairStudio: (() -> Unit)? = null

    init {
        setupItems()
    }

    private fun setupItems() {
        items.add(RadialItem("ZOOM") {
            // Passed 'context' to fix the compilation error
            ModuleManager.getModule("zoom")?.toggle(context)
        })
        items.add(RadialItem("PERSPECTIVE") {
            ModuleManager.getModule("perspective")?.toggle(context)
        })
        items.add(RadialItem("QUICK DROP") {
            ModuleManager.getModule("quick_drop")?.toggle(context)
        })
        items.add(RadialItem("CROSSHAIR") {
            onOpenCrosshairStudio?.invoke()
        })
        items.add(RadialItem("PACKS") {
            ModuleManager.getModule("pack_changer")?.toggle(context)
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        if (items.isEmpty()) return

        val sweepAngle = 360f / items.size
        val rectF = RectF(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)

        for (i in items.indices) {
            val startAngle = i * sweepAngle - 90f
            arcPaint.color = if (i == hoveredIndex) InkTheme.accentPrimary else InkTheme.bgPanel
            arcPaint.alpha = if (i == hoveredIndex) 230 else 180

            canvas.drawArc(rectF, startAngle, sweepAngle, true, arcPaint)
            canvas.drawArc(rectF, startAngle, sweepAngle, true, strokePaint)

            val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
            val labelRadius = (innerRadius + outerRadius) / 2f
            val textX = (cx + labelRadius * cos(midAngleRad)).toFloat()
            val textY = (cy + labelRadius * sin(midAngleRad)).toFloat() + 8f

            textPaint.color = if (i == hoveredIndex) Color.WHITE else InkTheme.textSecondary
            canvas.drawText(items[i].label, textX, textY, textPaint)
        }

        arcPaint.color = InkTheme.bgVoid
        arcPaint.alpha = 255
        canvas.drawCircle(cx, cy, innerRadius, arcPaint)
        canvas.drawCircle(cx, cy, innerRadius, strokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val dx = event.x - cx
        val dy = event.y - cy
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (dist in innerRadius..outerRadius) {
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                    if (angle < 0) angle += 360f
                    val sweepAngle = 360f / items.size
                    hoveredIndex = (angle / sweepAngle).toInt().coerceIn(0, items.size - 1)
                } else {
                    hoveredIndex = -1
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (hoveredIndex != -1 && dist in innerRadius..outerRadius) {
                    items[hoveredIndex].action.invoke()
                }
                hoveredIndex = -1
                visibility = GONE
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
