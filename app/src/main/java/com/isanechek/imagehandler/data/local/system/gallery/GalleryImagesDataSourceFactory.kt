package com.isanechek.imagehandler.data.local.system.gallery

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.models.GalleryRequest
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.models.ProgressState
import kotlinx.coroutines.CoroutineScope

class GalleryImagesDataSourceFactory(
    context: Context,
    scope: CoroutineScope,
    galleryManager: GalleryManager,
    request: GalleryRequest,
    progressState: (String) -> Unit
) : DataSource.Factory<Int, Image>() {

    private val source = GalleryImagesDataSource(
        context,
        scope,
        galleryManager,
        request,
        progressState
    )
    private val liveDataSource = MutableLiveData<PositionalDataSource<Image>>()

    override fun create(): DataSource<Int, Image> {
        liveDataSource.postValue(source)
        return source
    }

}