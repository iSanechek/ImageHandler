package com.isanechek.imagehandler.di

import androidx.room.Room
import com.isanechek.imagehandler.data.local.database.CacheDatabase
import com.isanechek.imagehandler.data.local.database.ImagesDatabase
import com.isanechek.imagehandler.data.local.database.LocalDatabase2
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            ImagesDatabase::class.java,
            "vova_helper.db"
        ).fallbackToDestructiveMigration().build()
    }

    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            LocalDatabase2::class.java,
            "vova.db"
        ).fallbackToDestructiveMigration().build()
    }

    single {
        Room.inMemoryDatabaseBuilder(
            androidApplication().applicationContext,
            CacheDatabase::class.java
        ).fallbackToDestructiveMigration().build()
    }

    factory {
        get<CacheDatabase>().imagesDao()
    }

    factory {
        get<LocalDatabase2>().imageDao2()
    }

    factory {
        get<ImagesDatabase>().watermarkDao()
    }

    factory {
        get<ImagesDatabase>().galleryDao()
    }

    factory {
        get<ImagesDatabase>().citiesDao()
    }
}