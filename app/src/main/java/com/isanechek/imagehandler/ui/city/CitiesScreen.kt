package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.cities_screen_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CitiesScreen : BaseFragment(_layout.cities_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()

    override fun bindUi(savedInstanceState: Bundle?) {
        vm.loadCities2()
        csl_toolbar.apply {
            title = "Города"
            setBackOrCloseButton { findNavController().navigateUp() }
        }

        val citiesAdapter = CitiesAdapter()

        with(csl_list) {
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

        csl_edit_btn.onClick {
            MaterialDialog(requireContext()).show {
                input(hint = "введите город", allowEmpty = true) { _, text ->
                    vm.saveCity(text.toString())
                }
                negativeButton(text = "закрыть")
                positiveButton(text = "сохранить")
            }
        }
    }
}