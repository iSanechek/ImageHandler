package com.isanechek.imagehandler.ui.watermarks

import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.data.local.database.entity.WatermarkChoicesImageEntity
import com.isanechek.imagehandler.databinding.WatermarkListItemLayoutBinding
import com.isanechek.imagehandler.onClick
import java.io.File

class WatermarksListHolder(val containerView: WatermarkListItemLayoutBinding) :
    RecyclerView.ViewHolder(containerView.root) {

    fun bindTo(item: WatermarkChoicesImageEntity, callback: (WatermarkChoicesImageEntity) -> Unit) {
        containerView.wliContainer.onClick { callback.invoke(item) }
        containerView.wliIv.load(File(item.path))
        containerView.wliTv.text = item.title

        if (item.isSelected) {
            if (containerView.wliSelectedTv.isInvisible) containerView.wliSelectedTv.isInvisible =
                false
        } else {
            if (!containerView.wliSelectedTv.isInvisible) containerView.wliSelectedTv.isInvisible =
                true
        }
    }
}