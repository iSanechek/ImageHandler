package com.isanechek.imagehandler.ui.choices.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler._color
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.databinding.ChoicesListItemLayoutBinding
import java.io.File

class ChoicesAdapter : RecyclerView.Adapter<ChoicesAdapter.ChoicesHolder>() {

    class ChoicesHolder(val containerView: ChoicesListItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        private val itemDetails =
            PickerItemDetails()

        fun bind(image: Image, isSelected: Boolean, position: Int) {
            itemDetails.key = image.id
            itemDetails.pos = position
            itemView.isActivated = isSelected
            with(containerView) {
                cliIv.apply {
                    load(File(image.path))
                    updateLayoutParams {
                        if (isSelected) {
                            cliContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.context, _color.colorAccent))
                            val params = this as ViewGroup.MarginLayoutParams
                            params.setMargins(8, 8, 8, 8)
                        } else {
                            cliContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.context, _color.colorWhite))
                            val params = this as ViewGroup.MarginLayoutParams
                            params.setMargins(0,0,0,0)
                        }
                    }
                }
            }
        }

        fun getDetails(): ItemDetailsLookup.ItemDetails<Long> = itemDetails
    }

    private val items = mutableListOf<Image>()
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoicesHolder =
        ChoicesHolder(ChoicesListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChoicesHolder, position: Int) {
        tracker?.let { selected ->
            val item = items[position]
            holder.bind(item, selected.isSelected(item.id), position)
        }
    }

    fun submit(data: List<Image>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}