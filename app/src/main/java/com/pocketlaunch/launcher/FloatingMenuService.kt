package com.pocketlaunch.launcher

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView

class FloatingMenuService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: FrameLayout
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 1. Create the container for our floating button
        floatingView = FrameLayout(this)

        // 2. Configure Window Manager parameters for overlay drawing
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        // 3. Create the Floating Button View
        val guiButton = ImageView(this).apply {
            val sizeInPx = (56 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(sizeInPx, sizeInPx)
            setPadding(16, 16, 16, 16)
            setImageResource(android.R.drawable.ic_menu_preferences)
        }

        // 4. Apply Glassmorphism Background & Glowing Cyan Border
        val glassDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#CC111827")) // Translucent dark background
            setStroke(4, Color.parseColor("#00E5FF")) // Glowing Cyan border
        }
        guiButton.background = glassDrawable

        // 5. Add Drag & Touch Feedback (Animation + Menu Toggle)
        guiButton.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true

                        // Shrink animation when pressed
                        view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            isClick = false
                        }

                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()

                        if (isClick && event.action == MotionEvent.ACTION_UP) {
                            view.performClick()
                            openLauncherMenu()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView.addView(guiButton)
        windowManager.addView(floatingView, params)
    }

    private fun openLauncherMenu() {
        // Trigger overlay panel here
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
