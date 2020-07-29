package com.isanechek.imagehandler.ui.handler.choices

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup

class SelectItemDetails : ItemDetailsLookup.ItemDetails<Long>() {

    var pos: Int = 0
    var key: Long = 0

    override fun getSelectionKey(): Long? = key

    override fun getPosition(): Int = pos

    override fun inDragRegion(e: MotionEvent): Boolean = true

    override fun inSelectionHotspot(e: MotionEvent): Boolean = true
}