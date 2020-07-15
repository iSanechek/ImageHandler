package com.isanechek.imagehandler

import android.app.Application
import com.isanechek.imagehandler.data.dataModule
import com.isanechek.imagehandler.data.local.database.databaseModule
import com.isanechek.imagehandler.ui.uiModule
import com.isanechek.imagehandler.ui.watermarks.watermarkModule
import com.isanechek.imagehandler.utils.utilsModule
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
                        uiModule + utilsModule
            )
        }
    }
}