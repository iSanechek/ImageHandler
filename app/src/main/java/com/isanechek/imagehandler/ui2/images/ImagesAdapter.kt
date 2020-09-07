package com.isanechek.imagehandler.ui2.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.databinding.ImageItemLayoutBinding
import com.isanechek.imagehandler.toFile
import com.isanechek.imagehandler.ui.base.BaseViewHolder
import com.isanechek.imagehandler.ui.base.ItemClickCallback

class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.ImageHolder>() {

    class ImageHolder(val containerView: ImageItemLayoutBinding) :
        BaseViewHolder<Image>(containerView.root) {

        override fun bindTo(data: Image, OnClick: (ItemClickCallback<Image>) -> Unit) {
            containerView.iiCover.load(data.path.toFile())
        }

    }

    private val items = mutableListOf<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder = ImageHolder(
        ImageItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.bindTo(items[position], OnClick = {})
    }

    fun submit(data: List<Image>) {
        with(items) {
            if (isNotEmpty()) clear()
            addAll(data)
        }
        notifyDataSetChanged()
    }
}