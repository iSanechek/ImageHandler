package com.isanechek.imagehandler.ui.widgets.fab

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.isanechek.imagehandler.R
import com.isanechek.imagehandler.ui.widgets.fab.FabUtil.OnFabValueCallback
import com.isanechek.imagehandler.ui.widgets.fab.FabUtil.createIndeterminateAnimator
import com.isanechek.imagehandler.ui.widgets.fab.FabUtil.createProgressAnimator
import com.isanechek.imagehandler.ui.widgets.fab.FabUtil.createStartAngleAnimator


class ProgressRingView : View, OnFabValueCallback {
    var TAG = ProgressRingView::class.java.simpleName
    private var progressPaint: Paint? = null
    private var size = 0
    private var bounds: RectF? = null
    private val boundsPadding = 0.14f
    private var viewRadius = 0
    private var ringWidthRatio = 0.14f //of a possible 1f;
    private var indeterminate = false
    private var autostartanim = false
    private var progress = 0f
    private var maxProgress = 0f
    private var indeterminateSweep = 0f
    private var indeterminateRotateOffset = 0f
    private var ringWidth = 0
    private var midRingWidth = 0
    private var animDuration = 0
    private var progressColor = Color.BLACK

    // Animation related stuff
    private var startAngle = 0f
    private var actualProgress = 0f
    private var startAngleRotate: ValueAnimator? = null
    private var progressAnimator: ValueAnimator? = null
    private var indeterminateAnimator: AnimatorSet? = null
    private var fabViewListener: CircleImageView.OnFabViewListener? = null

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init(attrs, 0)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)
        progress = a.getFloat(R.styleable.CircleImageViewR_android_progress, 0f)
        progressColor = a.getColor(R.styleable.CircleImageViewR_fbb_progressColor, progressColor)
        maxProgress = a.getFloat(R.styleable.CircleImageViewR_android_max, 100f)
        indeterminate = a.getBoolean(R.styleable.CircleImageViewR_android_indeterminate, false)
        autostartanim = a.getBoolean(R.styleable.CircleImageViewR_fbb_autoStart, true)
        animDuration = a.getInteger(R.styleable.CircleImageViewR_android_indeterminateDuration, 4000)
        ringWidthRatio =
            a.getFloat(R.styleable.CircleImageViewR_fbb_progressWidthRatio, ringWidthRatio)
        a.recycle()
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint!!.color = progressColor
        progressPaint!!.style = Paint.Style.STROKE
        progressPaint!!.strokeCap = Paint.Cap.BUTT
        if (autostartanim) {
            startAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = Math.min(w, h)
        viewRadius = size / 2
        setRingWidth(-1, true)
    }

    fun setRingWidthRatio(ringWidthRatio: Float) {
        this.ringWidthRatio = ringWidthRatio
    }

    fun setAutostartanim(autostartanim: Boolean) {
        this.autostartanim = autostartanim
    }

    fun setFabViewListener(fabViewListener: CircleImageView.OnFabViewListener?) {
        this.fabViewListener = fabViewListener
    }

    fun setRingWidth(width: Int, original: Boolean) {
        ringWidth = if (original) {
            Math.round(viewRadius.toFloat() * ringWidthRatio)
        } else {
            width
        }
        midRingWidth = ringWidth / 2
        progressPaint!!.strokeWidth = ringWidth.toFloat()
        updateBounds()
    }

    private fun updateBounds() {
        bounds = RectF(
            midRingWidth.toFloat(),
            midRingWidth.toFloat(),
            (size - midRingWidth).toFloat(),
            (size - midRingWidth).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the arc
        val sweepAngle =
            if (isInEditMode) progress / maxProgress * 360 else actualProgress / maxProgress * 360
        if (!indeterminate) {
            canvas.drawArc(bounds!!, startAngle, sweepAngle, false, progressPaint!!)
        } else {
            canvas.drawArc(
                bounds!!,
                startAngle + indeterminateRotateOffset,
                indeterminateSweep,
                false,
                progressPaint!!
            )
        }
    }

    /**
     * Sets the progress of the progress bar.
     * @param currentProgress the current progress you want to set
     */
    fun setProgress(currentProgress: Float) {
        progress = currentProgress
        // Reset the determinate animation to approach the new progress
        if (!indeterminate) {
            if (progressAnimator != null && progressAnimator!!.isRunning) {
                progressAnimator!!.cancel()
            }
            progressAnimator =
                createProgressAnimator(this, actualProgress, currentProgress, this)
            progressAnimator!!.start()
        }
        invalidate()
    }

    fun setMaxProgress(maxProgress: Float) {
        this.maxProgress = maxProgress
    }

    fun setIndeterminate(indeterminate: Boolean) {
        this.indeterminate = indeterminate
    }

    fun setAnimDuration(animDuration: Int) {
        this.animDuration = animDuration
    }

    fun setProgressColor(progressColor: Int) {
        this.progressColor = progressColor
        progressPaint!!.color = progressColor
    }

    /**
     * Starts the progress bar animation.
     * (This is an alias of resetAnimation() so it does the same thing.)
     */
    fun startAnimation() {
        resetAnimation()
    }

    fun stopAnimation(hideProgress: Boolean) {
        if (startAngleRotate != null && startAngleRotate!!.isRunning) {
            startAngleRotate!!.cancel()
        }
        if (progressAnimator != null && progressAnimator!!.isRunning) {
            progressAnimator!!.cancel()
        }
        if (indeterminateAnimator != null && indeterminateAnimator!!.isRunning) {
            indeterminateAnimator!!.cancel()
        }
        if (hideProgress) {
            setRingWidth(0, false)
        } else {
            setRingWidth(0, true)
        }
        invalidate()
    }

    /**
     * Resets the animation.
     */
    fun resetAnimation() {
        stopAnimation(false)
        // Determinate animation
        if (!indeterminate) {
            // The cool 360 swoop animation at the start of the animation
            startAngle = -90f
            startAngleRotate = createStartAngleAnimator(this, -90f, 270f, this)
            startAngleRotate!!.start()
            // The linear animation shown when progress is updated
            actualProgress = 0f
            progressAnimator = createProgressAnimator(this, actualProgress, progress, this)
            progressAnimator!!.start()
        } else { // Indeterminate animation
            startAngle = -90f
            indeterminateSweep = FabUtil.INDETERMINANT_MIN_SWEEP
            // Build the whole AnimatorSet
            indeterminateAnimator = AnimatorSet()
            var prevSet: AnimatorSet? = null
            var nextSet: AnimatorSet
            for (k in 0 until FabUtil.ANIMATION_STEPS) {
                nextSet = createIndeterminateAnimator(this, k.toFloat(), animDuration, this)
                val builder =
                    indeterminateAnimator!!.play(nextSet)
                if (prevSet != null) {
                    builder.after(prevSet)
                }
                prevSet = nextSet
            }

            // Listen to end of animation so we can infinitely loop
            indeterminateAnimator!!.addListener(object : AnimatorListenerAdapter() {
                var wasCancelled = false
                override fun onAnimationCancel(animation: Animator) {
                    wasCancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!wasCancelled) {
                        resetAnimation()
                    }
                }
            })
            indeterminateAnimator!!.start()
        }
    }

    override fun onIndeterminateValuesChanged(
        indeterminateSweep: Float,
        indeterminateRotateOffset: Float,
        startAngle: Float,
        progress: Float
    ) {
        if (indeterminateSweep != -1f) {
            this.indeterminateSweep = indeterminateSweep
        }
        if (indeterminateRotateOffset != -1f) {
            this.indeterminateRotateOffset = indeterminateRotateOffset
        }
        if (startAngle != -1f) {
            this.startAngle = startAngle
        }
        if (progress != -1f) {
            actualProgress = progress
            if (Math.round(actualProgress) == 100 && fabViewListener != null) {
                fabViewListener?.onProgressCompleted()
            }
        }
    }
}