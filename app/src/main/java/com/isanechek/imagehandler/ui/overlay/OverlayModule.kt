package com.isanechek.imagehandler.ui.overlay

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val overlayModule = module {

    viewModel {
        OverlayViewModel(androidApplication(), get())
    }
}