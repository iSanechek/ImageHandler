package com.isanechek.imagehandler

import android.app.Application
import com.isanechek.imagehandler.data.dataModule
import com.isanechek.imagehandler.data.local.database.databaseModule
import com.isanechek.imagehandler.ui.imagehandler.imageHandlerModule
import com.isanechek.imagehandler.ui.main.mainModule
import com.isanechek.imagehandler.ui.overlay.testModule
import com.isanechek.imagehandler.ui.uiModule
import com.isanechek.imagehandler.ui.watermarks.watermarkModule
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
                mainModule +
                imageHandlerModule +
                databaseModule +
                dataModule +
                watermarkModule +
                testModule +
                uiModule
            )
        }
    }
}