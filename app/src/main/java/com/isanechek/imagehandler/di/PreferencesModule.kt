package com.isanechek.imagehandler.di

import android.content.Context
import android.content.SharedPreferences
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.local.system.PrefManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesModule = module {
    single {
        androidContext()
            .applicationContext
            .getSharedPreferences("imagehandler", Context.MODE_PRIVATE)
    } bind (SharedPreferences::class)

    single<PrefManager> {
        PrefManagerImpl(get())
    }
}