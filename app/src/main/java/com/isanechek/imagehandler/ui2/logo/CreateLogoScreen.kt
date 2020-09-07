package com.isanechek.imagehandler.ui2.logo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.databinding.CreateLogoScreenLayoutBinding
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui2.logo.viewmodels.CreateLogoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CreateLogoScreen : BaseFragment(_layout.create_logo_screen_layout) {

    private val binding by viewBinding(CreateLogoScreenLayoutBinding::bind)
    private val vm: CreateLogoViewModel by viewModel()

    override fun bindUi(savedInstanceState: Bundle?) {
        setupButton()
        vm.selectedCity.observe(this, Observer(::observerSelectedCity))
        vm.uiState.observe(this, Observer(::observeUiState))
    }

    private fun observeUiState(state: UiState<String>) {
        when (state) {
            is UiState.Error -> {
                debugLog { "STATE ERROR ${state.message}" }
            }
            is UiState.Done -> {
                binding.clsResultIv.load(File(state.data))
            }
            is UiState.Progress -> {
            }
            is UiState.Permission -> Unit
        }
    }

    private fun observerSelectedCity(city: City) {
        debugLog { "CITY $city" }
        binding.clsSelectCity.text = city.name
        vm.generateLogo(city)
    }

    private fun setupButton() {
        binding.clsSelectCity.onClick {
            findNavController().navigate(_id.go_create_logo_to_cities_select)
        }
    }

}