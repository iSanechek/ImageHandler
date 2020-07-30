package com.isanechek.imagehandler.di

import androidx.room.Room
import com.isanechek.imagehandler.data.local.database.CacheDatabase
import com.isanechek.imagehandler.data.local.system.*
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManagerImpl
import com.isanechek.imagehandler.data.repositories.*
import com.isanechek.imagehandler.service.GalleryJobContract
import com.isanechek.imagehandler.service.GalleryJobScheduler
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dataModule = module {

    factory<FilesManager> {
        FilesManagerImpl()
    }

    factory<OverlayManager> {
        OverlayManagerImpl()
    }

    factory<MediaStoreManager> {
        MediaStoreManagerImpl()
    }

    single<WatermarkRepository> {
        WatermarkRepositoryImpl(
            get(),
            get(),
            get()
        )
    }

    single<WatermarkPhotosRepository> {
        WatermarkPhotosRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<ChoicesRepository> { ChoicesRepositoryImpl(get(), get()) }

    factory<GalleryManager> { GalleryManagerImpl() }

    single<GalleryJobContract> {
        GalleryJobScheduler()
    }

    single<ImagesRepository> {
        ImagesRepositoryImpl(get(), get())
    }

    factory<DashboardRepository> {
        DashboardRepositoryImpl(get())
    }
}