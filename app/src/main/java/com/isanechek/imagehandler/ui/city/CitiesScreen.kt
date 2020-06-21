package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.cities_screen_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CitiesScreen : BaseFragment(_layout.cities_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()

    override fun bindUi(savedInstanceState: Bundle?) {
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

                }

                override fun unchecked(city: City) {

                }

                override fun click(city: City) {

                }

                override fun longClick(city: City) {

                }
            })
        }

        vm.loadCities().observe(this, Observer { data ->
            citiesAdapter.submit(data)
        })

        csl_edit_btn.onClick {
            MaterialDialog(requireContext()).show {
                input(hint = "введите город", allowEmpty = true) { _, text ->
                    vm.saveSelectedCity(text.toString())
                }
                negativeButton(text = "закрыть")
                positiveButton(text = "сохранить")
            }
        }
    }
}