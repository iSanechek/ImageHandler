package com.isanechek.imagehandler.ui.splash

import android.os.Bundle
import com.isanechek.imagehandler.BuildConfig
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.SplashFragmentLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ui.base.BaseFragment

class SplashFragment : BaseFragment(_layout.splash_fragment_layout) {

    private val binding by viewBinding(SplashFragmentLayoutBinding::bind)

    override fun bindUi(savedInstanceState: Bundle?) {
        val text = "версия ${BuildConfig.VERSION_NAME}"
        binding.ssBuildVersion.text = text
    }

}