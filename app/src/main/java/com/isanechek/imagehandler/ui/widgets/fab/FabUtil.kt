package com.isanechek.imagehandler.ui.widgets.fab

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator


/**
 * Created by kurt on 21 02 2015 .
 */
object FabUtil {
    const val INDETERMINANT_MIN_SWEEP = 15f
    const val ANIMATION_STEPS = 4

    /**
     * Creates the starting angle animator for the circleview
     * @param view the view that the animator is to be attached too
     * @param from animate from value
     * @param to   animate to value
     * @param callback the callback interface for animations
     * @return ValueAnimator instance
     */
    fun createStartAngleAnimator(
        view: View,
        from: Float,
        to: Float,
        callback: OnFabValueCallback
    ): ValueAnimator {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = 5000
        animator.interpolator = DecelerateInterpolator(2f)
        animator.addUpdateListener { animation ->
            val startAngle = animation.animatedValue as Float
            callback.onIndeterminateValuesChanged(-1f, -1f, startAngle, -1f)
            view.invalidate()
        }
        return animator
    }

    /**
     * Creates a progress animator
     * @param view the view that the animator is to be attached too
     * @param from animate from value
     * @param to   animate to value
     * @param callback the callback interface for animations
     * @return ValueAnimator instance
     */
    fun createProgressAnimator(
        view: View,
        from: Float,
        to: Float,
        callback: OnFabValueCallback
    ): ValueAnimator {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = 500
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val actualProgress = animation.animatedValue as Float
            callback.onIndeterminateValuesChanged(-1f, -1f, -1f, actualProgress)
            view.invalidate()
        }
        return animator
    }

    /**
     * Creates a progress animator
     * @param view the view that the animator is to be attached too
     * @param step animation steps of the prgress animation
     * @param animDuration   duration of the animation i.e. 1 cycle
     * @param callback the callback interface for animations
     * @return AnimatorSet instance
     */
    fun createIndeterminateAnimator(
        view: View,
        step: Float,
        animDuration: Int,
        callback: OnFabValueCallback
    ): AnimatorSet {
        val maxSweep =
            360f * (ANIMATION_STEPS - 1) / ANIMATION_STEPS + INDETERMINANT_MIN_SWEEP
        val start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP)
        // Extending the front of the arc
        val frontEndExtend =
            ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep)
        frontEndExtend.duration = animDuration / ANIMATION_STEPS / 2.toLong()
        frontEndExtend.interpolator = DecelerateInterpolator(1f)
        frontEndExtend.addUpdateListener { animation ->
            val indeterminateSweep = animation.animatedValue as Float
            callback.onIndeterminateValuesChanged(indeterminateSweep, -1f, -1f, -1f)
            view.invalidate()
        }

        // Overall rotation
        val rotateAnimator1 = ValueAnimator.ofFloat(
            step * 720f / ANIMATION_STEPS,
            (step + .5f) * 720f / ANIMATION_STEPS
        )
        rotateAnimator1.duration = animDuration / ANIMATION_STEPS / 2.toLong()
        rotateAnimator1.interpolator = LinearInterpolator()
        rotateAnimator1.addUpdateListener { animation ->
            val indeterminateRotateOffset =
                animation.animatedValue as Float
            callback.onIndeterminateValuesChanged(-1f, indeterminateRotateOffset, -1f, -1f)
        }

        // Retracting the back end of the arc
        val backEndRetract =
            ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP)
        backEndRetract.duration = animDuration / ANIMATION_STEPS / 2.toLong()
        backEndRetract.interpolator = DecelerateInterpolator(1f)
        backEndRetract.addUpdateListener { animation ->
            val startAngle = animation.animatedValue as Float
            val indeterminateSweep = maxSweep - startAngle + start
            callback.onIndeterminateValuesChanged(indeterminateSweep, -1f, startAngle, -1f)
            view.invalidate()
        }

        // More overall rotation
        val rotateAnimator2 = ValueAnimator.ofFloat(
            (step + .5f) * 720f / ANIMATION_STEPS,
            (step + 1) * 720f / ANIMATION_STEPS
        )
        rotateAnimator2.duration = animDuration / ANIMATION_STEPS / 2.toLong()
        rotateAnimator2.interpolator = LinearInterpolator()
        rotateAnimator2.addUpdateListener { animation ->
            val indeterminateRotateOffset =
                animation.animatedValue as Float
            callback.onIndeterminateValuesChanged(-1f, indeterminateRotateOffset, -1f, -1f)
        }
        val set = AnimatorSet()
        set.play(frontEndExtend).with(rotateAnimator1)
        set.play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1)
        return set
    }

    /**
     * the animation callback interface that should be used by classes that want to listen for events from the library animations
     */
    interface OnFabValueCallback {
        fun onIndeterminateValuesChanged(
            indeterminateSweep: Float,
            indeterminateRotateOffset: Float,
            startAngle: Float,
            progress: Float
        )
    }
}