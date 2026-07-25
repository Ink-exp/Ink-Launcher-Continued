package com.pocketlaunch.launcher.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

data class WaypointMarker(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    var colorHex: Int = Color.parseColor("#7C3AED")
)

/**
 * Renders 3D coordinate waypoints and distance tags on the 2D HUD canvas.
 */
class WaypointOverlayRenderer {

    private val activeWaypoints = mutableListOf<WaypointMarker>()

    private val markerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
    }

    fun addWaypoint(waypoint: WaypointMarker) {
        activeWaypoints.add(waypoint)
    }

    fun clearWaypoints() {
        activeWaypoints.clear()
    }

    fun renderWaypoints(
        canvas: Canvas,
        playerX: Double,
        playerY: Double,
        playerZ: Double,
        screenWidth: Int,
        screenHeight: Int
    ) {
        for ((index, waypoint) in activeWaypoints.withIndex()) {
            val dx = waypoint.x - playerX
            val dy = waypoint.y - playerY
            val dz = waypoint.z - playerZ
            val distance = Math.sqrt(dx * dx + dy * dy + dz * dz).toInt()

            val screenX = (screenWidth / 2f) + (index * 60f) - 30f
            val screenY = 180f

            markerPaint.color = waypoint.colorHex
            canvas.drawCircle(screenX, screenY, 14f, markerPaint)

            val label = "${waypoint.name} [${distance}m]"
            canvas.drawText(label, screenX - (textPaint.measureText(label) / 2f), screenY + 36f, textPaint)
        }
    }
}
