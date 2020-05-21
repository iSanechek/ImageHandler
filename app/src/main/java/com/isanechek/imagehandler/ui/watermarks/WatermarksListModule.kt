package com.isanechek.imagehandler.ui.watermarks

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val watermarkModule = module {

    viewModel {
        WatermarksListViewModel(androidApplication(), get(), get(), get())
    }
}