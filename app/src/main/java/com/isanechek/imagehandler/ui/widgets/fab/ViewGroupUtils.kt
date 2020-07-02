package com.isanechek.imagehandler.ui.widgets.fab

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup


/**
 * Created by kurt on 2015/06/08.
 */
object ViewGroupUtils {
    private var IMPL: ViewGroupUtilsImpl? = null
    fun offsetDescendantRect(
        parent: ViewGroup?,
        descendant: View?,
        rect: Rect?
    ) {
        IMPL!!.offsetDescendantRect(parent, descendant, rect)
    }

    fun getDescendantRect(
        parent: ViewGroup?,
        descendant: View,
        out: Rect
    ) {
        out[0, 0, descendant.width] = descendant.height
        offsetDescendantRect(parent, descendant, out)
    }

    private class ViewGroupUtilsImplHoneycomb : ViewGroupUtilsImpl {
        override fun offsetDescendantRect(
            parent: ViewGroup?,
            child: View?,
            rect: Rect?
        ) {
            ViewGroupUtilsHoneycomb.offsetDescendantRect(parent!!, child!!, rect!!)
        }
    }

    private class ViewGroupUtilsImplBase : ViewGroupUtilsImpl {
        override fun offsetDescendantRect(
            parent: ViewGroup?,
            child: View?,
            rect: Rect?
        ) {
            parent?.offsetDescendantRectToMyCoords(child, rect)
        }

    }

    private interface ViewGroupUtilsImpl {
        fun offsetDescendantRect(
            var1: ViewGroup?,
            var2: View?,
            var3: Rect?
        )
    }

    init {
        val version = Build.VERSION.SDK_INT
        IMPL = if (version >= 11) {
            ViewGroupUtilsImplHoneycomb()
        } else {
            ViewGroupUtilsImplBase()
        }
    }
}