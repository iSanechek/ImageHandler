package com.isanechek.imagehandler.ui.city

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.City
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.city_item_layout.*

class CitiesAdapter :
    RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

    interface Callback {
        fun checked(city: City)
        fun unchecked(city: City)
    }

    inner class CityHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(city: City, callback: Callback?) {
            eci_title.text = city.name
            setupCard(city.isSelected)
//            eci_selected.apply {
//                setImageDrawable(selectedDrawable(city.isSelected))
//                onClick {
//                    if (city.isSelected) {
//                        callback?.unchecked(city)
//                    } else callback?.checked(city)
//                }
//            }

            eci_container.onClick {
                if (city.isSelected) {
                    callback?.unchecked(city)
                } else callback?.checked(city)
            }
        }

        private fun setupCard(selected: Boolean) {
            debugLog { "SElected $selected" }
            if (selected) {
                eci_container.strokeColor = ContextCompat.getColor(itemView.context, _color.colorAccent)
                eci_title.setTextColor(ContextCompat.getColor(itemView.context, _color.colorPrimaryText))
            } else {
                eci_container.strokeColor = ContextCompat.getColor(itemView.context, _color.colorCardBackground)
                eci_title.setTextColor(ContextCompat.getColor(itemView.context, _color.colorSecondaryText))
            }
        }

        private fun selectedDrawable(isSelected: Boolean): Drawable? =
            if (!isSelected) ContextCompat.getDrawable(
                itemView.context,
                _drawable.btn_checkbox_unchecked_to_checked_mtrl_animation
            ) else ContextCompat.getDrawable(
                itemView.context,
                _drawable.btn_checkbox_checked_to_unchecked_mtrl_animation
            )
    }
    private var callback: Callback? = null
    private val cities = mutableListOf<City>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        return CityHolder(parent.inflate(_layout.city_item_layout))
    }

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        holder.bind(cities[position], callback)
    }

    override fun getItemCount(): Int = cities.size


    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun submit(data: List<City>) {
        cities.apply {
            if (isNotEmpty()) clear()
            addAll(data)
        }
        notifyDataSetChanged()
    }
}