package com.pocketlaunch.launcher.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Smooth UI animations for opening/closing windows, drawers, and overlay dialogs.
 */
object UiAnimationUtils {

    fun scaleIn(view: View, durationMs: Long = 250L, onComplete: () -> Unit = {}) {
        view.visibility = View.VISIBLE
        view.scaleX = 0.85f
        view.scaleY = 0.85f
        view.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.85f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.85f, 1.0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1.0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = durationMs
            interpolator = OvershootInterpolator(1.2f)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
            start()
        }
    }

    fun scaleOut(view: View, durationMs: Long = 200L, onComplete: () -> Unit = {}) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 0.85f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 0.85f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    view.visibility = View.GONE
                    onComplete()
                }
            })
            start()
        }
    }

    /**
     * Springs [view] inward while the user holds it down. Every caller must pair this with
     * [releaseTapScale] on both ACTION_UP and ACTION_CANCEL, otherwise a gesture that gets
     * stolen (e.g. by the game surface) leaves the view stuck at [pressedScale].
     */
    fun pressTapScale(view: View, pressedScale: Float = 0.88f, durationMs: Long = 90L) {
        view.animate().cancel()
        view.animate()
            .scaleX(pressedScale)
            .scaleY(pressedScale)
            .setDuration(durationMs)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /** Releases the [pressTapScale] press, settling back to rest with a slight bounce. */
    fun releaseTapScale(view: View, durationMs: Long = 220L) {
        view.animate().cancel()
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(durationMs)
            .setInterpolator(OvershootInterpolator(2.0f))
            .start()
    }

    fun animateHeight(view: View, startHeightPx: Int, endHeightPx: Int, durationMs: Long = 200L) {
        ValueAnimator.ofInt(startHeightPx, endHeightPx).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val params = view.layoutParams
                params.height = animator.animatedValue as Int
                view.layoutParams = params
            }
            start()
        }
    }
}
