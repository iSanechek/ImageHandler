package com.isanechek.imagehandler.ui.choices.images

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler._color
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.choices_list_item_layout.*
import java.io.File

class ChoicesAdapter : RecyclerView.Adapter<ChoicesAdapter.ChoicesHolder>() {

    inner class ChoicesHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val itemDetails =
            PickerItemDetails()

        fun bind(image: Image, isSelected: Boolean, position: Int) {
            itemDetails.key = image.id
            itemDetails.pos = position
            itemView.isActivated = isSelected
            cli_iv.apply {
                load(File(image.path))
                updateLayoutParams {
                    if (isSelected) {
                        cli_container.setCardBackgroundColor(ContextCompat.getColor(itemView.context, _color.colorAccent))
                        val params = this as ViewGroup.MarginLayoutParams
                        params.setMargins(8, 8, 8, 8)
                    } else {
                        cli_container.setCardBackgroundColor(ContextCompat.getColor(itemView.context, _color.colorWhite))
                        val params = this as ViewGroup.MarginLayoutParams
                        params.setMargins(0,0,0,0)
                    }
                }
            }
        }

        fun getDetails(): ItemDetailsLookup.ItemDetails<Long> = itemDetails
    }

    private val items = mutableListOf<Image>()
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoicesHolder =
        ChoicesHolder(parent.inflate(_layout.choices_list_item_layout))

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