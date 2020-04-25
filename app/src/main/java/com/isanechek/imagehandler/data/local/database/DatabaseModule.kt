package com.isanechek.imagehandler.data.local.database

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.local.system.PrefManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            ImagesDatabase::class.java,
            "images.db"
        ).fallbackToDestructiveMigration().build()
    }

    factory {
        get<ImagesDatabase>().imageHandlerDao()
    }

    factory {
        get<ImagesDatabase>().watermarkDao()
    }

    factory {
        get<ImagesDatabase>().galleryDao()
    }

    /*Preferences*/

    single {
        androidContext()
            .applicationContext
            .getSharedPreferences("imagehandler", Context.MODE_PRIVATE)
    } bind (SharedPreferences::class)

    single<PrefManager> {
        PrefManagerImpl(get())
    }
}