package com.isanechek.imagehandler.ui.imagehandler

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val imageHandlerModule = module {
    viewModel {
        ImageHandlerViewModel(
            androidApplication(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}