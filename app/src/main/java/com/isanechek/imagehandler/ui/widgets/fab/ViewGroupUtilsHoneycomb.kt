package com.isanechek.imagehandler.ui.widgets.fab

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent


/**
 * Created by kurt on 2015/06/08.
 */
internal object ViewGroupUtilsHoneycomb {
    private val sMatrix =
        ThreadLocal<Matrix>()
    private val sRectF = ThreadLocal<RectF>()
    private val IDENTITY = Matrix()
    fun offsetDescendantRect(
        group: ViewGroup,
        child: View,
        rect: Rect
    ) {
        var m = sMatrix.get()
        if (m == null) {
            m = Matrix()
            sMatrix.set(m)
        } else {
            m.set(IDENTITY)
        }
        offsetDescendantMatrix(group, child, m)
        var rectF = sRectF.get()
        if (rectF == null) {
            rectF = RectF()
        }
        rectF.set(rect)
        m.mapRect(rectF)
        rect[(rectF.left + 0.5f).toInt(), (rectF.top + 0.5f).toInt(), (rectF.right + 0.5f).toInt()] =
            (rectF.bottom + 0.5f).toInt()
    }

    fun offsetDescendantMatrix(
        target: ViewParent,
        view: View,
        m: Matrix
    ) {
        val parent = view.parent
        if (parent is View && parent !== target) {
            val vp = parent as View
            offsetDescendantMatrix(target, vp, m)
            m.preTranslate(-vp.scrollX.toFloat(), -vp.scrollY.toFloat())
        }
        m.preTranslate(view.left.toFloat(), view.top.toFloat())
        if (!view.matrix.isIdentity) {
            m.preConcat(view.matrix)
        }
    }
}