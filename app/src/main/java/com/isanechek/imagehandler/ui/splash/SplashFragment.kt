package com.isanechek.imagehandler.ui.splash

import android.os.Bundle
import com.isanechek.imagehandler.BuildConfig
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.splash_fragment_layout.*

class SplashFragment : BaseFragment(_layout.splash_fragment_layout) {

    override fun bindUi(savedInstanceState: Bundle?) {
        val text = "alpha version\nверсия.${BuildConfig.VERSION_NAME}"
        ss_build_version.text = text
    }

}