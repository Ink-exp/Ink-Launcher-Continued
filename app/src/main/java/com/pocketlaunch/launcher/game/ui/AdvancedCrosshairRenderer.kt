package com.pocketlaunch.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Advanced Crosshair Configuration Model
 */
data class CrosshairConfig(
    var style: CrosshairStyle = CrosshairStyle.CROSS,
    var length: Float = 14f,
    var thickness: Float = 3f,
    var gap: Float = 5f,
    var opacity: Float = 1.0f,
    var rotationAngle: Float = 0f,
    
    // Dot settings
    var showDot: Boolean = true,
    var dotSize: Float = 2.5f,
    var dotOutline: Boolean = true,

    // Outline settings
    var showOutline: Boolean = true,
    var outlineThickness: Float = 1.5f,
    var outlineColor: Int = Color.BLACK,
    var outlineOpacity: Float = 1.0f,

    // Colors & Rainbow
    var primaryColor: Int = Color.parseColor("#00FF66"),
    var isRainbow: Boolean = false,
    var rainbowSpeed: Float = 2.0f,

    // Dynamic Reactivity (Recoil/Movement expansion)
    var dynamicSpread: Boolean = true,
    var maxSpreadExpansion: Float = 8f,

    // Hit Marker
    var enableHitMarker: Boolean = true,
    var hitMarkerColor: Int = Color.RED,
    var hitMarkerDurationMs: Long = 120L
)

enum class CrosshairStyle {
    CROSS, T_SHAPE, DOT_ONLY, CIRCLE, SQUARE, 
    FLARIAL_PRO, RETICLE_DIAMOND, VIRTUAL_DOT
}

/**
 * Enterprise-grade Crosshair Renderer handling advanced shapes, shadows, and smooth scaling.
 */
class AdvancedCrosshairRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var config = CrosshairConfig()
    var currentSpreadOffset: Float = 0f
    var isTargetHit: Boolean = false

    private var hitMarkerTimer: Long = 0L
    private var rainbowHue: Float = 0f

    private val linePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val outlinePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // 1. Resolve Active Color (Rainbow vs Static vs HitMarker)
        val paintColor = resolveActiveColor()
        
        linePaint.color = paintColor
        linePaint.alpha = (config.opacity * 255).toInt()
        linePaint.strokeWidth = config.thickness

        outlinePaint.color = config.outlineColor
        outlinePaint.alpha = (config.outlineOpacity * 255).toInt()
        outlinePaint.strokeWidth = config.thickness + (config.outlineThickness * 2f)

        fillPaint.color = paintColor
        fillPaint.alpha = (config.opacity * 255).toInt()

        canvas.save()
        canvas.rotate(config.rotationAngle, cx, cy)

        val totalGap = config.gap + (if (config.dynamicSpread) currentSpreadOffset else 0f)

        // 2. Render based on Selected Style
        when (config.style) {
            CrosshairStyle.CROSS -> renderCrossStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.T_SHAPE -> renderTShapeStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.DOT_ONLY -> renderDotOnly(canvas, cx, cy)
            CrosshairStyle.CIRCLE -> renderCircleStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.SQUARE -> renderSquareStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.FLARIAL_PRO -> renderFlarialProStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.RETICLE_DIAMOND -> renderDiamondStyle(canvas, cx, cy, totalGap)
            CrosshairStyle.VIRTUAL_DOT -> renderVirtualDotStyle(canvas, cx, cy)
        }

        // 3. Render Center Dot
        if (config.showDot && config.style != CrosshairStyle.DOT_ONLY && config.style != CrosshairStyle.VIRTUAL_DOT) {
            if (config.dotOutline && config.showOutline) {
                fillPaint.color = config.outlineColor
                canvas.drawCircle(cx, cy, config.dotSize + 1.2f, fillPaint)
                fillPaint.color = paintColor
            }
            canvas.drawCircle(cx, cy, config.dotSize, fillPaint)
        }

        // 4. Render Hit Marker Overlay Feedback
        if (config.enableHitMarker && isTargetHit) {
            if (System.currentTimeMillis() - hitMarkerTimer > config.hitMarkerDurationMs) {
                isTargetHit = false
            } else {
                renderHitMarker(canvas, cx, cy)
            }
        }

        canvas.restore()

        if (config.isRainbow || config.dynamicSpread) {
            postInvalidateDelayed(16) // Continuous 60 FPS update for rainbow animation
        }
    }

    private fun resolveActiveColor(): Int {
        if (isTargetHit) return config.hitMarkerColor
        if (config.isRainbow) {
            rainbowHue = (rainbowHue + config.rainbowSpeed) % 360f
            return Color.HSVToColor(floatArrayOf(rainbowHue, 1.0f, 1.0f))
        }
        return config.primaryColor
    }

    private fun renderCrossStyle(canvas: Canvas, cx: Float, cy: Float, gap: Float) {
        val len = config.length
        
        // Outlines
        if (config.showOutline) {
            canvas.drawLine(cx, cy - gap, cx, cy - gap - len, outlinePaint)
            canvas.drawLine(cx, cy + gap, cx, cy + gap + len, outlinePaint)
            canvas.drawLine(cx - gap, cy, cx - gap - len, cy, outlinePaint)
            canvas.drawLine(cx + gap, cy, cx + gap + len, cy, outlinePaint)
        }

        // Main Lines
        canvas.drawLine(cx, cy - gap, cx, cy - gap - len, linePaint)
        canvas.drawLine(cx, cy + gap, cx, cy + gap + len, linePaint)
        canvas.drawLine(cx - gap, cy, cx - gap - len, cy, linePaint)
        canvas.drawLine(cx + gap, cy, cx + gap + len, cy, linePaint)
    }

    private fun renderTShapeStyle(canvas: Canvas, cx: Float, cy: Float, gap: Float) {
        val len = config.length
        if (config.showOutline) {
            canvas.drawLine(cx, cy + gap, cx, cy + gap + len, outlinePaint)
            canvas.drawLine(cx - gap, cy, cx - gap - len, cy, outlinePaint)
            canvas.drawLine(cx + gap, cy, cx + gap + len, cy, outlinePaint)
        }
        canvas.drawLine(cx, cy + gap, cx, cy + gap + len, linePaint)
        canvas.drawLine(cx - gap, cy, cx - gap - len, cy, linePaint)
        canvas.drawLine(cx + gap, cy, cx + gap + len, cy, linePaint)
    }

    private fun renderDotOnly(canvas: Canvas, cx: Float, cy: Float) {
        if (config.dotOutline && config.showOutline) {
            fillPaint.color = config.outlineColor
            canvas.drawCircle(cx, cy, config.length + 1.5f, fillPaint)
            fillPaint.color = linePaint.color
        }
        canvas.drawCircle(cx, cy, config.length, fillPaint)
    }

    private fun renderCircleStyle(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        if (config.showOutline) {
            canvas.drawCircle(cx, cy, radius, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius, linePaint)
    }

    private fun renderSquareStyle(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val half = size + config.length
        if (config.showOutline) {
            canvas.drawRect(cx - half, cy - half, cx + half, cy + half, outlinePaint)
        }
        canvas.drawRect(cx - half, cy - half, cx + half, cy + half, linePaint)
    }

    private fun renderFlarialProStyle(canvas: Canvas, cx: Float, cy: Float, gap: Float) {
        renderCrossStyle(canvas, cx, cy, gap)
        val bracketSize = 6f
        if (config.showOutline) {
            canvas.drawRect(cx - bracketSize - gap, cy - bracketSize - gap, cx - gap, cy - gap, outlinePaint)
            canvas.drawRect(cx + gap, cy - bracketSize - gap, cx + bracketSize + gap, cy - gap, outlinePaint)
        }
        canvas.drawRect(cx - bracketSize - gap, cy - bracketSize - gap, cx - gap, cy - gap, linePaint)
        canvas.drawRect(cx + gap, cy - bracketSize - gap, cx + bracketSize + gap, cy - gap, linePaint)
    }

    private fun renderDiamondStyle(canvas: Canvas, cx: Float, cy: Float, d: Float) {
        path.reset()
        path.moveTo(cx, cy - config.length - d)
        path.lineTo(cx + config.length + d, cy)
        path.lineTo(cx, cy + config.length + d)
        path.lineTo(cx - config.length - d, cy)
        path.close()

        if (config.showOutline) canvas.drawPath(path, outlinePaint)
        canvas.drawPath(path, linePaint)
    }

    private fun renderVirtualDotStyle(canvas: Canvas, cx: Float, cy: Float) {
        if (config.showOutline) {
            canvas.drawCircle(cx, cy, config.length, outlinePaint)
            canvas.drawCircle(cx, cy, config.length * 2f, outlinePaint)
        }
        canvas.drawCircle(cx, cy, config.length, linePaint)
        canvas.drawCircle(cx, cy, config.length * 2f, linePaint)
    }

    private fun renderHitMarker(canvas: Canvas, cx: Float, cy: Float) {
        val mSize = 10f
        val hmPaint = Paint(linePaint).apply {
            color = config.hitMarkerColor
            strokeWidth = 3.5f
        }
        canvas.drawLine(cx - mSize, cy - mSize, cx - mSize / 2f, cy - mSize / 2f, hmPaint)
        canvas.drawLine(cx + mSize, cy - mSize, cx + mSize / 2f, cy - mSize / 2f, hmPaint)
        canvas.drawLine(cx - mSize, cy + mSize, cx - mSize / 2f, cy + mSize / 2f, hmPaint)
        canvas.drawLine(cx + mSize, cy + mSize, cx + mSize / 2f, cy + mSize / 2f, hmPaint)
    }

    fun triggerHitMarker() {
        isTargetHit = true
        hitMarkerTimer = System.currentTimeMillis()
        invalidate()
    }
}
