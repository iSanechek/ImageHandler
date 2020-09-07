package com.isanechek.imagehandler.ui.handler.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.databinding.SelectPreviewItemLayoutBinding
import java.io.File

class SelectPreviewAdapter(private val items: List<Image>, private val callback: (Image) -> Unit) :
    RecyclerView.Adapter<SelectPreviewAdapter.SelectPreviewHolder>() {

    class SelectPreviewHolder(val containerView: SelectPreviewItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bind(item: Image, callback: (Image) -> Unit) {
            containerView.spiCover.load(File(item.path))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectPreviewHolder {
        return SelectPreviewHolder(
            SelectPreviewItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SelectPreviewHolder, position: Int) {
        holder.bind(items[position], callback)
    }

    override fun getItemCount(): Int = items.size
}