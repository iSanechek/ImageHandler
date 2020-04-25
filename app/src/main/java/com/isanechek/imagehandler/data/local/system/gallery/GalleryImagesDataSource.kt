package com.isanechek.imagehandler.data.local.system.gallery

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.models.GalleryRequest
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.models.ProgressState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryImagesDataSource(
    private val context: Context,
    private val scope: CoroutineScope,
    private val galleryManager: GalleryManager,
    private val request: GalleryRequest,
    private val callbackState: (String) -> Unit
) : PositionalDataSource<Image>() {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Image>) {
        callbackState.invoke("loading")
        scope.launch(Dispatchers.IO) {
            val result = galleryManager.loadLastImages(
                context,
                request.folderName,
                params.requestedLoadSize,
                params.requestedStartPosition
            )
            when {
                result.isNotEmpty() -> callbackState.invoke("done")
                else -> callbackState.invoke("error")
            }
            callback.onResult(result, 0)
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Image>) {
        scope.launch(Dispatchers.IO) {
            val result = galleryManager.loadLastImages(
                context,
                request.folderName,
                params.loadSize,
                params.startPosition
            )
            when {
                result.isNotEmpty() -> callbackState.invoke("done")
                else -> callbackState.invoke("done")
            }
            callback.onResult(result)
        }
    }
}