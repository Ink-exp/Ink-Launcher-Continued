package com.pocketlaunch.launcher.ui

import android.graphics.Canvas

/**
 * Base abstract class for any draggable HUD widget on screen.
 */
abstract class HudElement(
    val id: String,
    val displayName: String,
    var xRatio: Float = 0.05f, // Position as percentage of screen width (0.0 to 1.0)
    var yRatio: Float = 0.05f, // Position as percentage of screen height (0.0 to 1.0)
    var scale: Float = 1.0f
) {
    var widthPx: Float = 200f
    var heightPx: Float = 60f
    var isSelected: Boolean = false

    abstract fun render(canvas: Canvas, currentX: Float, currentY: Float)

    /**
     * Checks if a touch point falls inside this HUD element's bounds.
     */
    fun containsPoint(touchX: Float, touchY: Float, screenWidth: Int, screenHeight: Int): Boolean {
        val absoluteX = xRatio * screenWidth
        val absoluteY = yRatio * screenHeight
        return touchX >= absoluteX && touchX <= (absoluteX + widthPx * scale) &&
               touchY >= absoluteY && touchY <= (absoluteY + heightPx * scale)
    }
}
