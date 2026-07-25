package com.pocketlaunch.launcher.ui

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistently saves and loads HUD widget positions and scales.
 */
class HudLayoutManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ink_hud_layout", Context.MODE_PRIVATE)

    fun saveElementPosition(element: HudElement) {
        prefs.edit().apply {
            putFloat("${element.id}_xRatio", element.xRatio)
            putFloat("${element.id}_yRatio", element.yRatio)
            putFloat("${element.id}_scale", element.scale)
            apply()
        }
    }

    fun loadElementPosition(element: HudElement) {
        element.xRatio = prefs.getFloat("${element.id}_xRatio", element.xRatio)
        element.yRatio = prefs.getFloat("${element.id}_yRatio", element.yRatio)
        element.scale = prefs.getFloat("${element.id}_scale", element.scale)
    }
}
