package com.isanechek.imagehandler.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.isanechek.imagehandler.d
import kotlinx.coroutines.coroutineScope

class WatermarkWorker(context: Context, params: WorkerParameters) : BaseWorker(context, params) {



    override suspend fun doWork(): Result = coroutineScope {
        try {

            val paths = inputData.getStringArray(ARGS)

            if (!paths.isNullOrEmpty()) {
                d { "Paths size ${paths.size}" }



            } else {
                d { "Paths empty!" }
                Result.failure()
            }

            Result.success()
        } catch (ex: Throwable) {
            Result.failure()
        }
    }

    companion object {
        const val ARGS = "watermark.worker.args"
    }

}