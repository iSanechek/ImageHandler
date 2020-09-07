package com.isanechek.imagehandler.ui2.logo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.databinding.SelectCityScreenLayout2Binding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ext.isOverZero
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui2.logo.adapters.SelectCitiesAdapter
import com.isanechek.imagehandler.ui2.logo.viewmodels.CitiesSelectViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CitiesSelectScreen : BaseFragment(_layout.select_city_screen_layout2) {

    private val binding by viewBinding(SelectCityScreenLayout2Binding::bind)
    private val vm: CitiesSelectViewModel by viewModel()
    private val citiesAdapter = SelectCitiesAdapter { item -> vm.setSelectedCity(item) }

    override fun bindUi(savedInstanceState: Bundle?) {
        setupList()
        vm.data.observe(this, Observer(::observerListData))
        vm.uiState.observe(this, Observer(::observerUiState))
    }

    private fun setupList() {
        with(binding.scs2List) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citiesAdapter
        }
    }

    private fun observerListData(data: List<City>) {
        citiesAdapter.submit(data)
    }

    private fun observerUiState(state: UiState<Int>) {
        when (state) {
            is UiState.Error -> {
            }
            is UiState.Done -> {
                if (state.data.isOverZero()) {
                    findNavController().navigateUp()
                }
            }
            is UiState.Progress -> {
            }
            is UiState.Permission -> Unit
        }
    }

}