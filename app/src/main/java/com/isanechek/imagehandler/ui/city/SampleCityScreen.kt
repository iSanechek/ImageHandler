package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import coil.api.load
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.SimpleCityScreenLayoutBinding
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SampleCityScreen : Fragment(_layout.simple_city_screen_layout) {

    private val binding by viewBinding(SimpleCityScreenLayoutBinding::bind)
    private val vm: SelectViewModel by sharedViewModel()

    private val args: String
        get() = arguments?.getString("sample_args") ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.loadPreviewState.observe(viewLifecycleOwner, Observer { state ->
            if (state) {
                binding.scslProgress.visibility = View.VISIBLE
            } else {
                binding.scslProgress.visibility = View.GONE
                binding.scslContentContainer.visibility = View.VISIBLE
            }
        })

        lifecycleScope.launchWhenStarted {
            vm.city.flowOn(Dispatchers.IO)
                .catch {
                    debugLog { "LOAD SELECTED CITY ERROR ${it.message}" }
                }.collect { city ->

                    if (city != null) {
                        debugLog { "CITY $city" }

                        vm.loadPreview(city.overlayPath)
                            .observe(viewLifecycleOwner, { bitmap ->
                                binding.scslReult.load(bitmap)
                            })

                        binding.scslSaveBtn.onClick {
                            vm.saveOverlay(city.overlayPath)
                            vm.goToScreen(-1)
                        }
                    } else debugLog { "CITY NULL" }
                }
        }
    }
}