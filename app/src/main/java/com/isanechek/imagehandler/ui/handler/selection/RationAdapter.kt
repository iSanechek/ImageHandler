package com.isanechek.imagehandler.ui.handler.selection

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.SelectionRationItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.ration_item_layout.*
import java.io.File

class RationAdapter(private val items: List<SelectionRationItem>) :
    RecyclerView.Adapter<RationAdapter.RationHolder>() {

    inner class RationHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: SelectionRationItem, callback: (SelectionRationItem) -> Unit) {
            debugLog { item.toString() }
            ril_title_format.text = item.title
            ril_description.text = item.description

            val set = ConstraintSet()
            when(item.ration) {
                ASPECT_RATIO_1_1 -> setRatio(set, "1:1")
                ASPECT_RATIO_16_9 -> setRatio(set, "16:9")
                ASPECT_RATIO_9_16 -> setRatio(set, "9:16")
            }
            ril_cover.load(File(item.coverPath))
        }

        private fun setRatio(set: ConstraintSet, ratio: String) {
            set.apply {
                clone(ril_container)
                setDimensionRatio(ril_cover.id, ratio)
                applyTo(ril_container)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RationHolder =
        RationHolder(parent.inflate(_layout.ration_item_layout))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RationHolder, position: Int) {
        holder.bind(items[position], callback = {})
    }

}