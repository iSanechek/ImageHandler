package com.isanechek.imagehandler.di

import com.isanechek.imagehandler.utils.TrackerUtils
import com.isanechek.imagehandler.utils.TrackerUtilsImpl
import org.koin.dsl.module

val utilsModule = module {

    single<TrackerUtils> {
        TrackerUtilsImpl()
    }
}