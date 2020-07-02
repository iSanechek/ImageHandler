package com.isanechek.imagehandler.ui.widgets.fab

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.DefaultBehavior
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.isanechek.imagehandler.R
import com.isanechek.imagehandler.ui.widgets.fab.CircleImageView.OnFabViewListener
import com.isanechek.imagehandler.ui.widgets.fab.ViewGroupUtils.getDescendantRect


/**
 * Created by kurt on 21 02 2015 .
 */
@DefaultBehavior(FabButton.Behavior::class)
class FabButton : FrameLayout, OnFabViewListener {
    private var circle: CircleImageView? = null
    private var ring: ProgressRingView? = null
    private var ringWidthRatio = 0.14f //of a possible 1f;
    private var indeterminate = false
    private var autostartanim = false
    private var endBitmapResource = 0
    private var showEndBitmap = false
    private var hideProgressOnComplete = false

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    protected fun init(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        val v =
            View.inflate(context, R.layout.widget_fab_button, this)
        clipChildren = false
        circle =
            v.findViewById<View>(R.id.fabbutton_circle) as CircleImageView
        ring = v.findViewById<View>(R.id.fabbutton_ring) as ProgressRingView
        circle!!.setFabViewListener(this)
        ring!!.setFabViewListener(this)
        var color = Color.BLACK
        var progressColor = Color.BLACK
        var animDuration = 4000
        var icon = -1
        var maxProgress = 0f
        var progress = 0f
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)
            color =
                a.getColor(R.styleable.CircleImageViewR_android_color, Color.BLACK)
            progressColor = a.getColor(
                R.styleable.CircleImageViewR_fbb_progressColor,
                Color.BLACK
            )
            progress = a.getFloat(R.styleable.CircleImageViewR_android_progress, 0f)
            maxProgress = a.getFloat(R.styleable.CircleImageViewR_android_max, 100f)
            indeterminate = a.getBoolean(R.styleable.CircleImageViewR_android_indeterminate, false)
            autostartanim = a.getBoolean(R.styleable.CircleImageViewR_fbb_autoStart, true)
            animDuration = a.getInteger(
                R.styleable.CircleImageViewR_android_indeterminateDuration,
                animDuration
            )
            icon = a.getResourceId(R.styleable.CircleImageViewR_android_src, icon)
            ringWidthRatio =
                a.getFloat(R.styleable.CircleImageViewR_fbb_progressWidthRatio, ringWidthRatio)
            endBitmapResource = a.getResourceId(
                R.styleable.CircleImageViewR_fbb_endBitmap,
                R.drawable.ic_fab_complete
            )
            showEndBitmap = a.getBoolean(R.styleable.CircleImageViewR_fbb_showEndBitmap, false)
            hideProgressOnComplete =
                a.getBoolean(R.styleable.CircleImageViewR_fbb_hideProgressOnComplete, false)
            circle!!.setShowShadow(a.getBoolean(R.styleable.CircleImageViewR_fbb_showShadow, true))
            a.recycle()
        }
        circle!!.setColor(color)
        circle!!.setShowEndBitmap(showEndBitmap)
        circle!!.setRingWidthRatio(ringWidthRatio)
        ring!!.setProgressColor(progressColor)
        ring!!.setProgress(progress)
        ring!!.setMaxProgress(maxProgress)
        ring!!.setAutostartanim(autostartanim)
        ring!!.setAnimDuration(animDuration)
        ring!!.setRingWidthRatio(ringWidthRatio)
        ring!!.setIndeterminate(indeterminate)
        if (icon != -1) {
            circle!!.setIcon(icon, endBitmapResource)
        }
    }

    fun setShadow(showShadow: Boolean) {
        circle!!.setShowShadow(showShadow)
    }

    fun setIcon(resource: Int, endIconResource: Int) {
        circle!!.setIcon(resource, endIconResource)
    }

    fun showShadow(show: Boolean) {
        circle!!.setShowShadow(show)
        invalidate()
    }

    fun setColor(color: Int) {
        circle!!.setColor(color)
    }

    fun setProgressColor(color: Int) {
        ring!!.setProgressColor(color)
    }

    fun setIcon(icon: Drawable?, endIcon: Drawable?) {
        circle!!.setIcon(icon!!, endIcon!!)
    }

    fun resetIcon() {
        circle!!.resetIcon()
    }

    /**
     * sets the progress to indeterminate or not
     *
     * @param indeterminate the flag
     */
    fun setIndeterminate(indeterminate: Boolean) {
        this.indeterminate = indeterminate
        ring!!.setIndeterminate(indeterminate)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        ring!!.setOnClickListener(listener)
        circle!!.setOnClickListener(listener)
    }

    /**
     * shows the animation ring
     *
     * @param show shows animation ring when set to true
     */
    fun showProgress(show: Boolean) {
        circle!!.showRing(show)
    }

    fun hideProgressOnComplete(hide: Boolean) {
        hideProgressOnComplete = hide
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        circle!!.isEnabled = enabled
        ring!!.isEnabled = enabled
    }

    /**
     * sets current progress
     *
     * @param progress the current progress to set value too
     */
    fun setProgress(progress: Float) {
        ring!!.setProgress(progress)
    }

    override fun onProgressVisibilityChanged(visible: Boolean) {
        if (visible) {
            ring!!.visibility = View.VISIBLE
            ring!!.startAnimation()
        } else {
            ring!!.stopAnimation(true)
            ring!!.visibility = View.GONE
        }
    }

    override fun onProgressCompleted() {
        circle!!.showCompleted(showEndBitmap, hideProgressOnComplete)
        if (hideProgressOnComplete) {
            ring!!.visibility = View.GONE
        }
    }

    class Behavior : CoordinatorLayout.Behavior<FabButton>() {
        // We only support the FAB <> Snackbar shift movement on Honeycomb and above. This is
        // because we can use view translation properties which greatly simplifies the code.
        private var mTmpRect: Rect? = null
        private var mIsAnimatingOut = false
        private var mTranslationY = 0f
        override fun layoutDependsOn(
            parent: CoordinatorLayout, child: FabButton,
            dependency: View
        ): Boolean {
            // We're dependent on all SnackbarLayouts (if enabled)
            return dependency is SnackbarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: FabButton,
            dependency: View
        ): Boolean {
            if (dependency is SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency)
            } else if (dependency is AppBarLayout) {
                if (mTmpRect == null) {
                    mTmpRect = Rect()
                }

                // First, let's get the visible rect of the dependency
                val rect: Rect = mTmpRect!!
                getDescendantRect(
                    parent,
                    dependency,
                    rect
                )
                if (rect.bottom <= getMinimumHeightForVisibleOverlappingContent(dependency)) {
                    // If the anchor's bottom is below the seam, we'll animate our FAB out
                    if (!mIsAnimatingOut && child.visibility == View.VISIBLE) {
                        animateOut(child)
                    }
                } else {
                    // Else, we'll animate our FAB back in
                    if (child.visibility != View.VISIBLE) {
                        animateIn(child)
                    }
                }
            }
            return false
        }

        fun getMinimumHeightForVisibleOverlappingContent(bar: AppBarLayout): Int {
            val topInset = 0
            val minHeight = ViewCompat.getMinimumHeight(bar)
            return if (minHeight != 0) {
                minHeight * 2 + topInset
            } else {
                val childCount = bar.childCount
                if (childCount >= 1) ViewCompat.getMinimumHeight(bar.getChildAt(childCount - 1)) * 2 + topInset else 0
            }
        }

        private fun updateFabTranslationForSnackbar(
            parent: CoordinatorLayout,
            fab: FabButton, snackbar: View
        ) {
            val translationY = getFabTranslationYForSnackbar(parent, fab)
            if (translationY != mTranslationY) {
                // First, cancel any current animation
                ViewCompat.animate(fab).cancel()
                if (Math.abs(translationY - mTranslationY) == snackbar.height
                        .toFloat()
                ) {
                    // If we're travelling by the height of the Snackbar then we probably need to
                    // animate to the value
                    ViewCompat.animate(fab)
                        .translationY(translationY)
                        .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(null)
                } else {
                    // Else we'll set use setTranslationY
                    ViewCompat.setTranslationY(fab, translationY)
                }
                mTranslationY = translationY
            }
        }

        private fun getFabTranslationYForSnackbar(
            parent: CoordinatorLayout,
            fab: FabButton
        ): Float {
            var minOffset = 0f
            val dependencies =
                parent.getDependencies(fab)
            var i = 0
            val z = dependencies.size
            while (i < z) {
                val view = dependencies[i]
                if (view is SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(
                        minOffset,
                        ViewCompat.getTranslationY(view) - view.getHeight()
                    )
                }
                i++
            }
            return minOffset
        }

        private fun animateIn(button: FabButton) {
            button.visibility = View.VISIBLE
            ViewCompat.animate(button)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                .withLayer()
                .setListener(null)
                .start()
        }

        private fun animateOut(button: FabButton) {
            ViewCompat.animate(button)
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                .withLayer()
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        mIsAnimatingOut = true
                    }

                    override fun onAnimationCancel(view: View) {
                        mIsAnimatingOut = false
                    }

                    override fun onAnimationEnd(view: View) {
                        mIsAnimatingOut = false
                        view.visibility = View.GONE
                    }
                }).start()
        }
    }
}