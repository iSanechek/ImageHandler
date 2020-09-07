package com.isanechek.imagehandler.ui.city

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.databinding.CityItemLayoutBinding

class CitiesAdapter :
    RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

    interface Callback {
        fun checked(city: City)
        fun unchecked(city: City)
    }

    inner class CityHolder(val containerView: CityItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bind(city: City, callback: Callback?) {
            containerView.eciTitle.text = city.name
            setupCard(city.isSelected)
//            eci_selected.apply {
//                setImageDrawable(selectedDrawable(city.isSelected))
//                onClick {
//                    if (city.isSelected) {
//                        callback?.unchecked(city)
//                    } else callback?.checked(city)
//                }
//            }

            containerView.eciContainer.onClick {
                if (city.isSelected) {
                    callback?.unchecked(city)
                } else callback?.checked(city)
            }
        }

        private fun setupCard(selected: Boolean) {
            debugLog { "SElected $selected" }
            if (selected) {
                containerView.eciContainer.strokeColor =
                    ContextCompat.getColor(itemView.context, _color.colorAccent)
                containerView.eciTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        _color.colorPrimaryText
                    )
                )
            } else {
                containerView.eciContainer.strokeColor =
                    ContextCompat.getColor(itemView.context, _color.colorCardBackground)
                containerView.eciTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        _color.colorSecondaryText
                    )
                )
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder = CityHolder(
        CityItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

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