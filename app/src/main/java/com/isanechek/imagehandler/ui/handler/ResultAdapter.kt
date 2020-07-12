package com.isanechek.imagehandler.ui.handler

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.ASPECT_RATIO_1_1
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.image_result_item_layout.*
import java.io.File
import java.util.*

class ResultAdapter : RecyclerView.Adapter<ResultAdapter.ResultHolder>() {

    inner class ResultHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(item: ImageItem) {
            debugLog { item.toString() }

            if (item.aspectRationOriginal != ASPECT_RATIO_1_1) {
                iri_warning.isVisible = true
            }

            val p = when(item.overlayStatus) {
                ImageItem.OVERLAY_DONE -> item.resultPath
                ImageItem.OVERLAY_NONE -> item.originalPath
                else -> item.originalPath
            }
            debugLog { "PATH $p" }
            iri_cover.load(File(p))
        }
    }

    private val items = mutableListOf<ImageItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder =
        ResultHolder(parent.inflate(_layout.image_result_item_layout))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submit(data: List<ImageItem>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        this.notifyDataSetChanged()
        debugLog { "BOOM ADAPTER" }
    }

    fun clear() {
        if (items.isNotEmpty()) items.clear()
    }
}