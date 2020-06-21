package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.select_city_screen_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectCityScreen : BaseFragment(_layout.select_city_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()

    override fun bindUi(savedInstanceState: Bundle?) {

        vm.loadSelectedCity().observe(this, Observer(::setupSelectedCity))


    }


    private fun setupSelectedCity(city: String) {
        if (city.isNotEmpty()) {
            setupSelectedBtn("изменить")
            scs_select_city.text = city
        } else {
            setupSelectedBtn("выбрать")
        }
    }

    private fun setupSelectedBtn(city: String) {
        scs_select_btn.apply {
            text = city
            onClick {
                findNavController().navigate(_id.go_select_to_cities)
            }
        }
    }

    private fun showCitiesDialog() {

    }

}