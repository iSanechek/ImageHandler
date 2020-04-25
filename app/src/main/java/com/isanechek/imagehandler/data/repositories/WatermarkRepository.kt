package com.isanechek.imagehandler.data.repositories

import android.content.Context
import android.graphics.BitmapFactory
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.dao.ImageHandlerDao
import com.isanechek.imagehandler.data.local.database.entity.ImageHandlerEntity
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.data.local.system.MediaStoreManager
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.models.ExecuteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

interface WatermarkRepository {
    suspend fun addData(paths: Array<String>?): Flow<ExecuteResult<String>>
    suspend fun saveResult(context: Context): Flow<ExecuteResult<String>>
}

class WatermarkRepositoryImpl(
    private val imageHandlerDao: ImageHandlerDao,
    private val mediaStoreManager: MediaStoreManager,
    private val filesManager: FilesManager,
    private val prefManager: PrefManager
) : WatermarkRepository {

    override suspend fun addData(paths: Array<String>?): Flow<ExecuteResult<String>> = flow {
        if (paths.isNullOrEmpty()) {
            emit(ExecuteResult.Error("Paths null or empty!"))
        } else {

        }
    }

    override suspend fun saveResult(context: Context): Flow<ExecuteResult<String>> = flow {
        emit(PROGRESS_STATE_LOAD_DATA_FROM_DB.toProgress())
        d { "BOOMMMM" }
        val data = imageHandlerDao.loadData()
        val appFolder = context.filesDir.absolutePath + File.separator + PRIVATE_APP_FOLDER_NAME
        if (data.isNotEmpty()) {
            if (prefManager.isFirstStart()) emit(PROGRESS_STATE_CHECK_PRIVATE_FOLDER.toProgress())
            val temp = mutableListOf<ImageHandlerEntity>()
            if (filesManager.createFolderIfEmpty(appFolder)) {
                emit(PROGRESS_STATE_EXECUTE_FILES_SIZE.toProgress(data.size))
                for (item in data) {
                    emit(PROGRESS_STATE_CHECK_EXISTS_FILE.toProgress(item.title))
                    if (filesManager.checkFileExists(item.resultPath)) {
                        val bitmap = BitmapFactory.decodeFile(item.resultPath)
                        emit(PROGRESS_STATE_SAVE_FILE.toProgress(item.title))
                        val (isOk, value) = mediaStoreManager.saveBitmapInGallery(
                            context = context,
                            fileName = item.title,
                            bitmap = bitmap,
                            folderName = PUBLIC_APP_FOLDER_NAME,
                            timestamp = item.lastUpdate
                        )

                        if (isOk) {
                            val i = item.copy(
                                status = ImageHandlerEntity.LABEL_IS_DONE,
                                publicPath = value
                            )
                            d { "TEST $i" }
                            temp.add(i)
                            emit(PROGRESS_STATE_SAVE_FILE_DONE.toProgress(item.title))
                        } else {
                            val i = item.copy(
                                status = ImageHandlerEntity.LABEL_IS_FAIL,
                                message = value
                            )
                            temp.add(i)
                        }
                    } else {
                        val i = item.copy(status = ImageHandlerEntity.LABEL_IS_FAIL)
                        temp.add(i)
                        emit(PROGRESS_STATE_SAVE_FILE_FAIL.toProgress(item.title))
                    }
                }

                if (temp.isNotEmpty()) {
                    imageHandlerDao.update(temp)
                    d { "hyui" }
                    emit(ExecuteResult.Done(EMPTY_VALUE))
                } else emit(ExecuteResult.Error(NOT_FIND_TO_SAVE))

            } else emit(ExecuteResult.Error("Create app folder fail! :("))
        } else emit(ExecuteResult.Error(DATABASE_EMPTY))
    }

    private fun String.toProgress(value: Any = "") = ExecuteResult.Progress(Pair(this, value))

}