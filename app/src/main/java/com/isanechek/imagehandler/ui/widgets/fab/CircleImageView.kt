package com.isanechek.imagehandler.ui.widgets.fab

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.isanechek.imagehandler.R


class CircleImageView : AppCompatImageView {
    interface OnFabViewListener {
        fun onProgressVisibilityChanged(visible: Boolean)
        fun onProgressCompleted()
    }

    private var centerY = 0
    private var centerX = 0
    private var viewRadius = 0
    private var progressVisible = false
    private var circleRadius = 0
    private var circlePaint: Paint? = null
    private var fabViewListener: OnFabViewListener? = null
    private val ringAlpha = 75
    private var ringRadius = 0
    private var ringPaint: Paint? = null
    private var currentRingWidth = 0f
    private var ringWidthRatio = 0.14f //of a possible 1f;
    private val drawables = arrayOfNulls<Drawable>(2)
    private var crossfader: TransitionDrawable? = null
    private var ringWidth = 0
    private var ringAnimator: ObjectAnimator? = null
    var shadowDy = 3.5f
    var shadowDx = 0f
    var shadowRadius = 10f
    var shadowTransparency = 100
    private var showEndBitmap = false
    private val showShadow = true

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    fun setFabViewListener(fabViewListener: OnFabViewListener?) {
        this.fabViewListener = fabViewListener
    }

    fun setShowShadow(showShadow: Boolean) {
        if (showShadow) {
            circlePaint!!.setShadowLayer(
                shadowRadius,
                shadowDx,
                shadowDy,
                Color.argb(shadowTransparency, 0, 0, 0)
            )
        } else {
            circlePaint!!.clearShadowLayer()
        }
        invalidate()
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        this.isFocusable = false
        this.scaleType = ScaleType.CENTER_INSIDE
        isClickable = true
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint!!.style = Paint.Style.FILL
        val displayMetrics = resources.displayMetrics
        shadowRadius = if (displayMetrics.densityDpi <= 240) {
            1.0f
        } else if (displayMetrics.densityDpi <= 320) {
            3.0f
        } else {
            10.0f
        }
        ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        ringPaint!!.style = Paint.Style.STROKE
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        var color = Color.BLACK
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)
            color =
                a.getColor(R.styleable.CircleImageViewR_android_color, Color.BLACK)
            ringWidthRatio =
                a.getFloat(R.styleable.CircleImageViewR_fbb_progressWidthRatio, ringWidthRatio)
            shadowRadius =
                a.getFloat(R.styleable.CircleImageViewR_android_shadowRadius, shadowRadius)
            shadowDy = a.getFloat(R.styleable.CircleImageViewR_android_shadowDy, shadowDy)
            shadowDx = a.getFloat(R.styleable.CircleImageViewR_android_shadowDx, shadowDx)
            setShowShadow(a.getBoolean(R.styleable.CircleImageViewR_fbb_showShadow, true))
            a.recycle()
        }
        setColor(color)
        val pressedAnimationTime = animationDuration
        ringAnimator = ObjectAnimator.ofFloat(this, "currentRingWidth", 0f, 0f)
        ringAnimator?.duration = pressedAnimationTime.toLong()
        ringAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (fabViewListener != null) {
                    fabViewListener!!.onProgressVisibilityChanged(progressVisible)
                }
            }
        })
    }

    fun setShowEndBitmap(showEndBitmap: Boolean) {
        this.showEndBitmap = showEndBitmap
    }

    /**
     * sets the icon that will be shown on the fab icon
     *
     * @param resource the resource id of the icon
     */
    fun setIcon(resource: Int, endBitmapResource: Int) {
        val srcBitmap = BitmapFactory.decodeResource(resources, resource)
        if (showEndBitmap) {
            val endBitmap = BitmapFactory.decodeResource(resources, endBitmapResource)
            setIconAnimation(
                BitmapDrawable(resources, srcBitmap),
                BitmapDrawable(resources, endBitmap)
            )
        } else {
            setImageBitmap(srcBitmap)
        }
    }

    /**
     * sets the icon that will be shown on the fab icon
     *
     * @param icon the initial icon
     * @param endIcon the icon to be displayed when the progress is finished
     */
    fun setIcon(icon: Drawable, endIcon: Drawable) {
        if (showEndBitmap) {
            setIconAnimation(icon, endIcon)
        } else {
            setImageDrawable(icon)
        }
    }

    private fun setIconAnimation(icon: Drawable, endIcon: Drawable) {
        drawables[0] = icon
        drawables[1] = endIcon
        crossfader = TransitionDrawable(drawables)
        crossfader!!.isCrossFadeEnabled = true
        setImageDrawable(crossfader)
    }

    fun resetIcon() {
        crossfader!!.resetTransition()
    }

    /**
     * this sets the thickness of the ring as a fraction of the radius of the circle.
     *
     * @param ringWidthRatio the ratio 0-1
     */
    fun setRingWidthRatio(ringWidthRatio: Float) {
        this.ringWidthRatio = ringWidthRatio
    }

    override fun onDraw(canvas: Canvas) {
        val ringR = ringRadius + currentRingWidth
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            ringR,
            ringPaint!!
        ) // the outer ring
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            circleRadius.toFloat(),
            circlePaint!!
        ) //the actual circle
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h / 2
        viewRadius = Math.min(w, h) / 2
        ringWidth = Math.round(viewRadius.toFloat() * ringWidthRatio)
        circleRadius = viewRadius - ringWidth
        ringPaint!!.strokeWidth = ringWidth.toFloat()
        ringPaint!!.alpha = ringAlpha
        ringRadius = circleRadius - ringWidth / 2
    }

    fun getCurrentRingWidth(): Float {
        return currentRingWidth
    }

    fun setCurrentRingWidth(currentRingWidth: Float) {
        this.currentRingWidth = currentRingWidth
        this.invalidate()
    }

    /**
     * sets the color of the circle
     *
     * @param color the actual color to set to
     */
    fun setColor(color: Int) {
        circlePaint!!.color = color
        ringPaint!!.color = color
        ringPaint!!.alpha = ringAlpha
        this.invalidate()
    }

    /**
     * whether to show the ring or not
     *
     * @param show set flag
     */
    fun showRing(show: Boolean) {
        progressVisible = show
        if (show) {
            ringAnimator!!.setFloatValues(currentRingWidth, ringWidth.toFloat())
        } else {
            ringAnimator!!.setFloatValues(ringWidth.toFloat(), 0f)
        }
        ringAnimator!!.start()
    }

    /**
     * this animates between the icon set in the imageview and the completed icon. does as crossfade animation
     *
     * @param show set flag
     * @param hideOnComplete if true animate outside ring out after progress complete
     */
    fun showCompleted(show: Boolean, hideOnComplete: Boolean) {
        if (show) {
            crossfader!!.startTransition(500)
        }
        if (hideOnComplete) {
            val hideAnimator =
                ObjectAnimator.ofFloat(this, "currentRingWidth", 0f, 0f)
            hideAnimator.setFloatValues(1f)
            hideAnimator.duration = animationDuration.toLong()
            hideAnimator.start()
        }
    }

    companion object {
        private const val animationDuration = 200
    }
}