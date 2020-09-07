package com.isanechek.imagehandler.ui2.logo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.databinding.SelectCityItemLayout2Binding
import com.isanechek.imagehandler.onClick

class SelectCitiesAdapter(private val clickCallback: (City) -> Unit) :
    RecyclerView.Adapter<SelectCitiesAdapter.SelectCityHolder>() {

    class SelectCityHolder(val containerView: SelectCityItemLayout2Binding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bind(item: City, clickCallback: (City) -> Unit) {
            containerView.sciContainer.onClick {
                clickCallback.invoke(item)
            }

            containerView.sciTitle.text = item.name
        }
    }

    private val items = mutableListOf<City>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCityHolder =
        SelectCityHolder(
            SelectCityItemLayout2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SelectCityHolder, position: Int) {
        holder.bind(items[position], clickCallback)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submit(data: List<City>) {
        items.apply {
            if (isNotEmpty()) clear()
            addAll(data)
        }
        notifyDataSetChanged()
    }
}