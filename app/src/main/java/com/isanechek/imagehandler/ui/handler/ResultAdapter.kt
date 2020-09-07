package com.isanechek.imagehandler.ui.handler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.databinding.ImageResultItemLayoutBinding
import java.io.File
import java.util.*

class ResultAdapter : RecyclerView.Adapter<ResultAdapter.ResultHolder>() {

    inner class ResultHolder(val containerView: ImageResultItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bind(item: ImageItem, callback: OnClickListener?) {
            debugLog { item.toString() }

            containerView.iriContainer.setOnLongClickListener {
                callback?.itemLongClick(item)
                true
            }

            containerView.iriContainer.onClick {
                callback?.itemClick(item)
            }

            if (item.aspectRationOriginal != ASPECT_RATIO_1_1) {
                containerView.iriWarning.isVisible = true
            }

            val p = when (item.overlayStatus) {
                ImageItem.OVERLAY_DONE -> item.resultPath
                ImageItem.OVERLAY_NONE -> item.originalPath
                else -> item.originalPath
            }
            debugLog { "PATH $p" }
            containerView.iriCover.load(File(p))
        }
    }

    private val items = mutableListOf<ImageItem>()
    private var clickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder =
        ResultHolder(
            ImageResultItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        holder.bind(items[position], clickListener)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.clickListener = listener
    }

    fun submit(data: List<ImageItem>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        this.notifyDataSetChanged()
        debugLog { "BOOM ADAPTER" }
    }

    fun clear() {
        if (items.isNotEmpty()) items.clear()
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun itemClick(item: ImageItem)
        fun itemLongClick(item: ImageItem)
    }
}