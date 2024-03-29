package com.isanechek.imagehandler.ui.handler.choices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler._drawable
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.databinding.SelectImgItemLayoutBinding
import com.isanechek.imagehandler.debugLog
import java.io.File

class SelectImgAdapter : RecyclerView.Adapter<SelectImgAdapter.SelectImgHolder>() {

    inner class SelectImgHolder(val containerView: SelectImgItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        private val MARGIN_SIZE = 16
        private val itemDetails = SelectItemDetails()

        fun bindTo(item: Image, isSelected: Boolean, position: Int) {
            itemDetails.apply {
                key = item.id
                pos = position
            }
            debugLog { "SELECTED $isSelected" }
            itemView.isActivated = isSelected
            containerView.sisiCover.load(File(item.path))

            containerView.sisiContainer.apply {
                if (isSelected) {
                    if (this.marginTop == 0) {
                        updateLayoutParams {
                            val params = this as ViewGroup.MarginLayoutParams
                            params.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
                            containerView.sisiContainer.layoutParams = params
                        }
                    }
                    containerView.sisiChecked.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            _drawable.ic_baseline_radio_button_checked_24
                        )
                    )
                } else {
                    containerView.sisiChecked.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            _drawable.ic_baseline_radio_button_unchecked_24
                        )
                    )
                    if (this.marginTop != 0) {
                        updateLayoutParams {
                            val params = this as ViewGroup.MarginLayoutParams
                            params.setMargins(0, 0, 0, 0)
                            containerView.sisiChecked.layoutParams = params
                        }
                    }
                }
            }
        }

        fun getDetails(): ItemDetailsLookup.ItemDetails<Long> = itemDetails
    }

    private var items = mutableListOf<Image>()
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectImgHolder =
        SelectImgHolder(
            SelectImgItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectImgHolder, position: Int) {
        tracker?.let { selected ->
            val item = items[position]
            holder.bindTo(item, selected.isSelected(item.id), position)
        }
    }

    fun submit(data: List<Image>) {
        if (items.isNotEmpty()) items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}