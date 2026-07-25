package com.pocketlaunch.launcher.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable

/**
 * InkClient Unified Design Tokens - Void Aesthetics & Modern Glassmorphism
 */
object InkTheme {
    val bgVoid = Color.parseColor("#06070B")
    val bgPanel = Color.parseColor("#0E1017")
    val bgCard = Color.parseColor("#151824")
    val bgCardHover = Color.parseColor("#1D2133")

    val borderDark = Color.parseColor("#1F2438")
    val borderLight = Color.parseColor("#2E3552")

    val accentPrimary = Color.parseColor("#7C3AED") // Electric Purple
    val accentSecondary = Color.parseColor("#3B82F6") // Cyber Blue
    val accentSuccess = Color.parseColor("#10B981") // Mint Green
    val accentDanger = Color.parseColor("#EF4444") // Coral Red

    val textPrimary = Color.parseColor("#FFFFFF")
    val textSecondary = Color.parseColor("#94A3B8")
    val textMuted = Color.parseColor("#64748B")

    fun createCardBackground(
        bgColor: Int = bgCard,
        borderColor: Int = borderDark,
        borderWidthPx: Int = 2,
        cornerRadiusPx: Float = 24f
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(bgColor)
            setStroke(borderWidthPx, borderColor)
            cornerRadius = cornerRadiusPx
        }
    }

    fun createGradientButton(
        startColor: Int = accentPrimary,
        endColor: Int = accentSecondary,
        cornerRadiusPx: Float = 18f
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = cornerRadiusPx
        }
    }

    /** Re-tints an existing token with [alpha] (0..255), keeping its RGB channels. */
    fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    /**
     * Glassmorphism squircle for floating overlay controls, stacked as concentric layers:
     * [glowRingCount] low-alpha accent rings that read as an outward glow, a bright
     * [accentPrimary] -> [accentSecondary] border, and a translucent [bgCard] glass face
     * with a vertical sheen.
     *
     * Rings are inset *inwards* from the view bounds, so size the view to include
     * [glowSpreadPx] + [borderWidthPx] of slack per side or the glass face ends up cramped.
     */
    fun createGlassGlowBackground(
        glassTopColor: Int = withAlpha(bgCardHover, 0xE6),
        glassBottomColor: Int = withAlpha(bgCard, 0xD4),
        glowStartColor: Int = accentPrimary,
        glowEndColor: Int = accentSecondary,
        cornerRadiusPx: Float = 30f,
        borderWidthPx: Int = 3,
        glowSpreadPx: Int = 8,
        glowRingCount: Int = 3,
        glowPeakAlpha: Int = 0x7A
    ): LayerDrawable {
        // Rings ramp from faint (outermost) to near-border strength. The alpha curve is
        // quadratic so the falloff reads as a soft halo rather than visible banding.
        val ringInsets = IntArray(glowRingCount) { i -> glowSpreadPx * i / glowRingCount }
        val glowRings = List(glowRingCount) { i ->
            val strength = (i + 1).toFloat() / glowRingCount
            val alpha = (glowPeakAlpha * strength * strength).toInt().coerceIn(1, 0xFF)
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(withAlpha(glowStartColor, alpha), withAlpha(glowEndColor, alpha))
            ).apply { cornerRadius = cornerRadiusPx - ringInsets[i] }
        }

        val accentBorder = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(glowStartColor, glowEndColor)
        ).apply { cornerRadius = cornerRadiusPx - glowSpreadPx }

        val glassFace = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(glassTopColor, glassBottomColor)
        ).apply { cornerRadius = cornerRadiusPx - glowSpreadPx - borderWidthPx }

        val layers = (glowRings + accentBorder + glassFace).toTypedArray<Drawable>()
        return LayerDrawable(layers).apply {
            ringInsets.forEachIndexed { i, inset -> setLayerInset(i, inset, inset, inset, inset) }
            setLayerInset(glowRingCount, glowSpreadPx, glowSpreadPx, glowSpreadPx, glowSpreadPx)
            val faceInset = glowSpreadPx + borderWidthPx
            setLayerInset(glowRingCount + 1, faceInset, faceInset, faceInset, faceInset)
        }
    }
}
