package com.isanechek.imagehandler.ui.city

import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.databinding.SelectCityScreenLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SelectCityScreen : BaseFragment(_layout.select_city_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()
    private val binding by viewBinding(SelectCityScreenLayoutBinding::bind)

    override fun bindUi(savedInstanceState: Bundle?) {

//        if (vm.isShowWarningDialog("sswd")) {
//            MaterialDialog(requireContext()).show {
//                title(text = "Предупреждение")
//                message(text = "Приложение находится в стадии разработки.")
//                positiveButton(text = "понятно") {
//                    vm.markDoneDialog("sswd")
//                }
//            }
//        }


        val citiesAdapter = CitiesAdapter()

        with(binding.scsList) {
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

        vm.progressState.observe(this, Observer { isShow ->
            if (isShow) {
                binding.scsInfoTv.visibility = View.VISIBLE
                binding.scsProgress.visibility = View.VISIBLE
            } else {
                binding.scsInfoTv.visibility = View.GONE
                binding.scsProgress.visibility = View.GONE
            }
        })

    }


    private fun setupSelectedCity(cityEntity: CityEntity?) {
        if (cityEntity != null) {
            if (!binding.scsActionContainer.isVisible) {
                binding.scsActionContainer.slideUp {}
                binding.scsSaveBtn.onClick {
                    debugLog { "CLICK CITY $cityEntity" }
                    vm.goToScreen(1)
                }
            }

            val text = String.format("Вы выбрали: %s", cityEntity.name)
            val spannable = SpannableString(text).apply {
                setSpan(
                    StyleSpan(BOLD),
                    12, this.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), _color.colorPrimaryText)),
                    12, this.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            binding.srcInfoText.text = spannable

        } else {
            if (binding.scsActionContainer.isVisible) {
                binding.scsActionContainer.slideDown {}
            }
        }
    }
}