package com.isanechek.imagehandler.ui.handler.choices

import androidx.recyclerview.selection.ItemKeyProvider
import com.isanechek.imagehandler.data.models.Image

class SelectItemKeyProvider : ItemKeyProvider<Long>(SCOPE_CACHED) {

    private val temp = mutableListOf<Image>()

    override fun getKey(position: Int): Long? = temp[position].id

    override fun getPosition(key: Long): Int = temp.indexOfFirst { i -> i.id == key }

    fun submit(data: List<Image>) {
        if (temp.isNotEmpty()) temp.clear()
        temp.addAll(data)
    }
}