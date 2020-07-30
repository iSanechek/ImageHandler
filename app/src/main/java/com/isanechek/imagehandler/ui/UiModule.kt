@file:JvmName("UiModule")

package com.isanechek.imagehandler.ui

import androidx.lifecycle.SavedStateHandle
import com.isanechek.imagehandler.ui.choices.ChoicesViewModel
import com.isanechek.imagehandler.ui.city.SelectViewModel
import com.isanechek.imagehandler.ui.crop.CropViewModel
import com.isanechek.imagehandler.ui.handler.ImageHandlerViewModel
import com.isanechek.imagehandler.ui.handler.choices.SelectImgViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {

    viewModel {
        ChoicesViewModel(androidApplication(), get(), get())
    }

    viewModel { (handle: SavedStateHandle) ->
        ImageHandlerViewModel(handle, androidApplication(), get(), get(), get(), get(), get())
    }

    viewModel {
        SelectViewModel(androidApplication(), get(), get(), get(), get())
    }

    viewModel {
        CropViewModel(androidApplication(), get(), get(), get(), get())
    }

    viewModel {
        SelectImgViewModel(androidApplication(), get())
    }
}