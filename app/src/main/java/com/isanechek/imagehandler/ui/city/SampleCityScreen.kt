package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.simple_city_screen_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

class SampleCityScreen : BaseFragment(_layout.simple_city_screen_layout) {

    private val vm: SelectViewModel by sharedViewModel()

    private val args: String
        get() = arguments?.getString("sample_args") ?: ""

    override fun bindUi(savedInstanceState: Bundle?) {

        debugLog { "ARGS $args" }

        lifecycleScope.launchWhenStarted {
            vm.city.flowOn(Dispatchers.IO)
                .catch {
                    debugLog { "LOAD SELECTED CITY ERROR ${it.message}" }
                }.collect { city ->
                    if (city != null) {
                        debugLog { "CITY $city" }

                        vm.loadPreview(city.overlayPath)
                            .observe(this@SampleCityScreen, Observer { bitmap ->
                                scsl_reult.load(bitmap)
                            })

                        scsl_save_btn.onClick {
                            vm.saveOverlay(city.overlayPath)
                            vm.goToScreen(-1)
                        }

                    } else debugLog { "CITY NULL" }
                }
        }
    }
}