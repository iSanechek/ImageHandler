package com.isanechek.imagehandler.ui.imagehandler

import android.view.View
import coil.api.load
import com.isanechek.imagehandler.data.local.database.entity.ImageHandlerEntity
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.toFile
import com.isanechek.imagehandler.ui.base.BaseViewHolder
import com.isanechek.imagehandler.ui.base.ItemClickCallback
import kotlinx.android.synthetic.main.image_handler_list_item_layout.*
import java.io.File

class ImageHandlerHolder(itemView: View) : BaseViewHolder<ImageHandlerEntity>(itemView) {

    override fun bindTo(
        data: ImageHandlerEntity,
        OnClick: (ItemClickCallback<ImageHandlerEntity>) -> Unit
    ) {
        ihli_container.apply {
            onClick { OnClick.invoke(ItemClickCallback.Click(data)) }
            setOnLongClickListener {
                OnClick.invoke(ItemClickCallback.LongClick(data))
                true
            }
        }

        if (data.resultPath.isNotEmpty() && data.resultPath.contains(".jpg")) {
            ihli_iv.load(data.resultPath.toFile())
        } else ihli_iv.load(data.originalPath.toFile())
    }
}