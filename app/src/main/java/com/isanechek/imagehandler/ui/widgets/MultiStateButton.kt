package com.isanechek.imagehandler.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.isanechek.imagehandler.*

class MultiStateButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var button: ImageButton
    private var progress: ProgressBar
    private var clickListener: OnClickListener? = null

    init {
        val container = this.inflate(_layout.multi_state_layout)
        button = container.findViewById(_id.msl_btn)
        button.onClick {
            clickListener?.onClick()
        }
        progress = container.findViewById(_id.msl_progress)
        addView(container)
    }

    fun setIcon(@DrawableRes iconId: Int) {
        button.setImageDrawable(ContextCompat.getDrawable(this.context, iconId))
    }

    fun setStateProgress(visible: Boolean) {
        debugLog { "VISIBLE $visible" }
        if (visible) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    fun showProgress() {
        if (progress.isInvisible) progress.isInvisible = false
        button.isClickable = false
    }

    fun hideProgress() {
        button.isClickable = true
        if (!progress.isInvisible) progress.isInvisible = true
    }

    fun setOnClickListener(clickListener: OnClickListener) {
        this.clickListener = clickListener
    }

    interface OnClickListener {
        fun onClick()
    }
}