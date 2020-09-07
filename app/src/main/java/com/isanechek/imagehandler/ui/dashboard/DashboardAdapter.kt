package com.isanechek.imagehandler.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.data.models.DashboardItem
import com.isanechek.imagehandler.databinding.DashboardItemLayoutBinding
import com.isanechek.imagehandler.onClick

class DashboardAdapter(private val items: List<DashboardItem>, private val callback: (DashboardItem) -> Unit) : RecyclerView.Adapter<DashboardAdapter.DashboardHolder>() {
    class DashboardHolder(val containerView: DashboardItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bindTo(item: DashboardItem, callback: (DashboardItem) -> Unit) {
            with(containerView) {
                with(dilContainer) {
                    onClick {
                        callback.invoke(item)
                    }
                    setBackgroundColor(ContextCompat.getColor(itemView.context, item.color))
                }
                dslIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context, item.icon))
                dslTitle.text = item.title
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardHolder =
        DashboardHolder(
            DashboardItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: DashboardHolder, position: Int) {
        holder.bindTo(items[position], callback)
    }

    override fun getItemCount(): Int = items.size
}