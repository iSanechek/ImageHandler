package com.isanechek.imagehandler.ui.watermarks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.database.entity.WatermarkChoicesImageEntity
import com.isanechek.imagehandler.databinding.WatermarkListItemLayoutBinding
import com.isanechek.imagehandler.inflate

class WatermarksListAdapter(private val callback: (WatermarkChoicesImageEntity) -> Unit) :
    RecyclerView.Adapter<WatermarksListHolder>() {

    private val items = mutableListOf<WatermarkChoicesImageEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatermarksListHolder =
        WatermarksListHolder(
            WatermarkListItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WatermarksListHolder, position: Int) {
        holder.bindTo(items[position], callback)
    }

    fun submit(data: List<WatermarkChoicesImageEntity>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

}