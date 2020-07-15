package com.isanechek.imagehandler.utils

import org.koin.dsl.module

val utilsModule = module {

    single<TrackerUtils> {
        TrackerUtilsImpl()
    }
}