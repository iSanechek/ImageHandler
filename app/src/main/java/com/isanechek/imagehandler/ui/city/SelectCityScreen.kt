package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.select_city_screen_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectCityScreen : BaseFragment(_layout.select_city_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()

    override fun bindUi(savedInstanceState: Bundle?) {

        val citiesAdapter = CitiesAdapter()

        with(scs_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citiesAdapter
        }

        citiesAdapter.apply {
            setCallback(object : CitiesAdapter.Callback {
                override fun checked(city: City) {
                    vm.updateCity(city.copy(isSelected = true))
                }

                override fun unchecked(city: City) {
                    vm.updateCity(city.copy(isSelected = false))
                }

                override fun click(city: City) {

                }

                override fun longClick(city: City) {
                    MaterialDialog(requireContext()).show {
                        title(text = "Предупреждение")
                        message(text = "Удалить ${city.name}")
                        negativeButton(text = "закрыть")
                        positiveButton(text = "удалить") {
                            vm.removeCity(city)
                        }
                    }
                }
            })
        }

        lifecycleScope.launchWhenCreated {
            vm.data.flowOn(Dispatchers.IO).collect { data ->
                citiesAdapter.submit(data)
            }
        }

        vm.errorState.observe(this, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        vm.loadSelectedCity().observe(this, Observer(::setupSelectedCity))

    }


    private fun setupSelectedCity(clickable: Boolean) {
        debugLog { "isClickable $clickable" }
        scs_save_btn.apply {
            isEnabled = clickable
            onClick {
                findNavController().navigate(_id.go_select_to_cities)
            }
        }
    }
}