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

/**
 * FloatingMenuService
 * Android Background Service managing the Ink Client floating trigger button
 * and slide-out HUD drawer lifecycle on screen.
 */
class FloatingMenuService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayContainer: FrameLayout
    private lateinit var triggerButton: ImageView
    private lateinit var clientDrawer: ClientHudDrawer

    private lateinit var buttonParams: WindowManager.LayoutParams
    private lateinit var drawerParams: WindowManager.LayoutParams

    private var isDrawerOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        clientDrawer = ClientHudDrawer(this)

        setupOverlayViews()
    }

    private fun setupOverlayViews() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // 1. Layout parameters for the Floating Trigger Button
        buttonParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 250
        }

        // 2. Layout parameters for the Slide-Out Client HUD Drawer
        drawerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        // Main Container for touch propagation
        overlayContainer = FrameLayout(this)

        // Create Minimalist Pill Trigger Button (Minecraft Client Style)
        triggerButton = ImageView(this).apply {
            val sizeInPx = (48 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(sizeInPx, sizeInPx)
            setPadding(12, 12, 12, 12)
            setImageResource(android.R.drawable.ic_menu_preferences)

            // Sleek obsidian pill styling with Minecraft green glow
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#E60F0F14"))
                setStroke(3, Color.parseColor("#55FF55"))
            }
        }

        setupButtonTouchEvents()

        overlayContainer.addView(triggerButton)
        windowManager.addView(overlayContainer, buttonParams)
    }

    private fun setupButtonTouchEvents() {
        triggerButton.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = buttonParams.x
                        initialY = buttonParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true

                        // Touch Feedback: Shrink slightly
                        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(60).start()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }

                        if (!isClick) {
                            buttonParams.x = initialX + dx
                            buttonParams.y = initialY + dy
                            windowManager.updateViewLayout(overlayContainer, buttonParams)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Bounce back to original scale
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start()

                        if (isClick && event.action == MotionEvent.ACTION_UP) {
                            view.performClick()
                            toggleClientDrawer()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun toggleClientDrawer() {
        if (isDrawerOpen) {
            closeClientDrawer()
        } else {
            openClientDrawer()
        }
    }

    private fun openClientDrawer() {
        if (isDrawerOpen) return
        
        val drawerLayout = clientDrawer.mainLayout
        if (drawerLayout.parent == null) {
            windowManager.addView(drawerLayout, drawerParams)
        }
        
        // Slide-in animation effect
        drawerLayout.translationX = -drawerLayout.width.toFloat()
        drawerLayout.animate().translationX(0f).setDuration(200).start()
        
        isDrawerOpen = true
    }

    private fun closeClientDrawer() {
        if (!isDrawerOpen) return

        val drawerLayout = clientDrawer.mainLayout
        drawerLayout.animate()
            .translationX(-drawerLayout.width.toFloat())
            .setDuration(180)
            .withEndAction {
                if (drawerLayout.parent != null) {
                    windowManager.removeView(drawerLayout)
                }
            }
            .start()

        isDrawerOpen = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayContainer.isInitialized && overlayContainer.parent != null) {
            windowManager.removeView(overlayContainer)
        }
        if (::clientDrawer.isInitialized && clientDrawer.mainLayout.parent != null) {
            windowManager.removeView(clientDrawer.mainLayout)
        }
    }
}
