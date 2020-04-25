package com.isanechek.imagehandler.ui.imagehandler

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.database.entity.ImageHandlerEntity
import com.isanechek.imagehandler.inflate
import com.isanechek.imagehandler.ui.base.ItemClickCallback

class ImageHandlerAdapter(
    private val callback: (ItemClickCallback<ImageHandlerEntity>) -> Unit
) : PagedListAdapter<ImageHandlerEntity, ImageHandlerHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHandlerHolder =
        ImageHandlerHolder(parent.inflate(_layout.image_handler_list_item_layout))

    override fun onBindViewHolder(holder: ImageHandlerHolder, position: Int) {
        getItem(position)?.let {
            holder.bindTo(it, OnClick = callback)
        }
    }


    companion object {
        private val diffCallback = object: DiffUtil.ItemCallback<ImageHandlerEntity>() {

            override fun areItemsTheSame(
                oldItem: ImageHandlerEntity,
                newItem: ImageHandlerEntity
            ): Boolean = oldItem.id == newItem.id && oldItem.resultPath == newItem.resultPath

            override fun areContentsTheSame(
                oldItem: ImageHandlerEntity,
                newItem: ImageHandlerEntity
            ): Boolean = oldItem == newItem

        }
    }
}