package com.isanechek.imagehandler.workers

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.isanechek.imagehandler.data.local.system.FilesManager
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.core.inject

class ClearWorker(
    appContext: Context,
    params: WorkerParameters
) : BaseWorker(appContext, params) {

    private val filesManager: FilesManager by inject()

    override suspend fun handleWork(): Result = coroutineScope {

        val cachePath = inputData.getString(CACHE_PATH_KEY) ?: ""
        debugLog { "CACHE PATH $cachePath" }
        if (cachePath.isEmpty()) {
            Result.failure()
        }

        withContext(Dispatchers.IO) {
            if (filesManager.checkFolderExistsAndIsNotEmpty(cachePath)) {
                val result = filesManager.clearAll(cachePath)
                debugLog { "CLEAR CACHE RESULT $result" }
            } else debugLog { "CACHE FOLDER EMPTY OR NOT EXISTS" }
        }

        Result.success()
    }

    companion object {
        const val CACHE_PATH_KEY = "c.p.k"
    }

}