@file:JvmName("OverlayModule")

package com.isanechek.imagehandler.ui.overlay

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {

    viewModel {
        OverlayViewModel(androidApplication(), get())
    }
}