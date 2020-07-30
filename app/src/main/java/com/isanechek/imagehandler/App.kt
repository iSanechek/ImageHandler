package com.isanechek.imagehandler

import android.app.Application
import com.isanechek.imagehandler.di.dataModule
import com.isanechek.imagehandler.di.databaseModule
import com.isanechek.imagehandler.di.preferencesModule
import com.isanechek.imagehandler.di.uiModule
import com.isanechek.imagehandler.ui.watermarks.watermarkModule
import com.isanechek.imagehandler.di.utilsModule
import glimpse.core.Glimpse
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Glimpse.init(this)

        startKoin {
            androidContext(this@App)
            modules(
                databaseModule +
                        dataModule +
                        watermarkModule +
                        uiModule +
                        utilsModule +
                        preferencesModule
            )
        }
    }
}