package com.isanechek.imagehandler.ui.handler.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.ASPECT_RATIO_16_9
import com.isanechek.imagehandler.ASPECT_RATIO_1_1
import com.isanechek.imagehandler.ASPECT_RATIO_9_16
import com.isanechek.imagehandler.data.models.SelectionRationItem
import com.isanechek.imagehandler.databinding.RationItemLayoutBinding
import com.isanechek.imagehandler.debugLog
import java.io.File

class RationAdapter(private val items: List<SelectionRationItem>) :
    RecyclerView.Adapter<RationAdapter.RationHolder>() {

    inner class RationHolder(val containerView: RationItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bind(item: SelectionRationItem, callback: (SelectionRationItem) -> Unit) {
            debugLog { item.toString() }
            containerView.rilTitleFormat.text = item.title
            containerView.rilDescription.text = item.description

            val set = ConstraintSet()
            when (item.ration) {
                ASPECT_RATIO_1_1 -> setRatio(set, "1:1")
                ASPECT_RATIO_16_9 -> setRatio(set, "16:9")
                ASPECT_RATIO_9_16 -> setRatio(set, "9:16")
            }
            containerView.rilCover.load(File(item.coverPath))
        }

        private fun setRatio(set: ConstraintSet, ratio: String) {
            set.apply {
                clone(containerView.rilContainer)
                setDimensionRatio(containerView.rilCover.id, ratio)
                applyTo(containerView.rilContainer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RationHolder =
        RationHolder(
            RationItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RationHolder, position: Int) {
        holder.bind(items[position], callback = {})
    }

}