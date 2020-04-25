@file:JvmName("UiModule")

package com.isanechek.imagehandler.ui

import com.isanechek.imagehandler.ui.choices.ChoicesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {

    viewModel {
        ChoicesViewModel(androidApplication(), get(), get())
    }
}