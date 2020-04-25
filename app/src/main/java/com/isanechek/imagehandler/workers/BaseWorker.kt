package com.isanechek.imagehandler.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.KoinComponent

abstract class BaseWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

}