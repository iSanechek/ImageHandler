package com.isanechek.imagehandler.ui.watermarks

import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.data.local.database.entity.WatermarkChoicesImageEntity
import com.isanechek.imagehandler.onClick
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.watermark_list_item_layout.*
import java.io.File

class WatermarksListHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bindTo(item: WatermarkChoicesImageEntity, callback: (WatermarkChoicesImageEntity) -> Unit) {
        wli_container.onClick { callback.invoke(item) }
        wli_iv.load(File(item.path))
        wli_tv.text = item.title

        if (item.isSelected) {
            if (wli_selected_tv.isInvisible) wli_selected_tv.isInvisible = false
        } else {
            if (!wli_selected_tv.isInvisible) wli_selected_tv.isInvisible = true
        }
    }
}