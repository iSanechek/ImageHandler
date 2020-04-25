package com.isanechek.imagehandler.ui.overlay.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.inflate
import com.isanechek.imagehandler.onClick
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.overlay_choices_dialog_item_layout.*
import java.io.File

class OverlayChoicesDialogAdapter :
    RecyclerView.Adapter<OverlayChoicesDialogAdapter.OverlayChoicesHolder>() {

    inner class OverlayChoicesHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(model: Image, callback: (Image) -> Unit) {
            ocdi_container.onClick { callback.invoke(model) }
            ocdi_iv.load(File(model.path))
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem == newItem
            }

        }
    }

    private val items = mutableListOf<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayChoicesHolder {
        return OverlayChoicesHolder(parent.inflate(_layout.overlay_choices_dialog_item_layout))
    }

    override fun onBindViewHolder(holder: OverlayChoicesHolder, position: Int) {
        holder.bind(items[position], callback = {})
    }

    override fun getItemCount(): Int = items.size


    fun submit(data: List<Image>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}