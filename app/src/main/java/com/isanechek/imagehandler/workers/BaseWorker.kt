package com.isanechek.imagehandler.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.KoinComponent

abstract class BaseWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    abstract suspend fun handleWork(): Result

    override suspend fun doWork(): Result = handleWork()
}