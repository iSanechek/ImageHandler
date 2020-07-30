package com.isanechek.imagehandler.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import com.isanechek.imagehandler.BuildConfig
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.data.repositories.WatermarkPhotosRepository
import com.isanechek.imagehandler.doWhile
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

interface GalleryJobContract {
    fun scheduleJob(context: Context)
    fun serviceIsRun(context: Context): Boolean
}

class GalleryJobScheduler : GalleryJobContract, JobService() {

    private val repository: WatermarkPhotosRepository by inject()
    private var jobInfo: JobInfo? = null
    private val parentJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + parentJob)

    override fun onStopJob(params: JobParameters?): Boolean {
        scope.coroutineContext.cancelChildren()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        params ?: return false
        scope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) { findUris(params) }
            if (result.isNotEmpty()) {
                repository.saveFindImages(result)
            }
            scheduleJob(this@GalleryJobScheduler)
            jobFinished(params, false)
        }
        return true
    }

    private suspend fun findUris(params: JobParameters): Set<Image> =
        suspendCancellableCoroutine { c ->
            val temp = mutableSetOf<Image>()
            if (params.triggeredContentAuthorities != null) {
                if (params.triggeredContentUris != null) {
                    val ids = mutableListOf<String>()
                    params.triggeredContentUris?.let { uris ->
                        for (uri in uris) {
                            val path = uri.pathSegments
                            if (path.size == EXTERNAL_PATH_SEGMENTS.size + 1) ids.add(path[path.size - 1])
                            if (ids.isNotEmpty()) {
                                val selection = StringBuilder()
                                for (i in 0 until ids.size) {
                                    if (selection.isNotEmpty()) selection.append(" OR ")

                                    selection.append(MediaStore.Images.ImageColumns._ID)
                                    selection.append("='")
                                    selection.append(ids[i])
                                    selection.append("'")

                                    try {
                                        contentResolver.query(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            PROJECTION,
                                            selection.toString(),
                                            null,
                                            null
                                        )?.use { cursor ->
                                            cursor.doWhile {
                                                val id = cursor.getLong(
                                                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                                                )
                                                val name =
                                                    cursor.getString(
                                                        cursor.getColumnIndexOrThrow(
                                                            MediaStore.MediaColumns.DISPLAY_NAME
                                                        )
                                                    )
                                                val fileUri =
                                                    ContentUris.withAppendedId(
                                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        id
                                                    )
                                                val date =
                                                    cursor.getLong(
                                                        cursor.getColumnIndexOrThrow(
                                                            MediaStore.MediaColumns.DATE_ADDED
                                                        )
                                                    )
                                                temp.add(Image(id = id, name = name, path = fileUri.toString(), addDate = date, folderName = ""))
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        debugLog { "Ошбка: Нет доступа к медиафайлу! ${e.message}" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            c.resume(temp)
        }

    @MainThread
    override fun scheduleJob(context: Context) {
        when {
            jobInfo != null -> a(context)
            else -> {
                val builder = JobInfo.Builder(
                    CHECKER_SERVICE_JOB_ID,
                    ComponentName(BuildConfig.APPLICATION_ID, GalleryJobScheduler::class.java.name)
                )

                builder.addTriggerContentUri(
                    JobInfo.TriggerContentUri(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
                    )
                )
                builder.addTriggerContentUri(JobInfo.TriggerContentUri(MEDIA_URI, 0))
                builder.setTriggerContentMaxDelay(500)
                jobInfo = builder.build()
                jobInfo?.let {
                    bindService(context).schedule(it)
                }
            }
        }
    }

    @MainThread
    override fun serviceIsRun(context: Context): Boolean {
        val jobs = bindService(context).allPendingJobs
        var isRunning = jobs.isNotEmpty()
        for (i in 0 until jobs.size) {
            if (jobs[i].id == CHECKER_SERVICE_JOB_ID) isRunning = true
        }
        return isRunning
    }

    fun a(context: Context): Int = bindService(context).schedule(jobInfo!!)

    private fun bindService(context: Context): JobScheduler =
        context.getSystemService(JobScheduler::class.java) as JobScheduler

    companion object {
        private const val CHECKER_SERVICE_JOB_ID = 999
        val MEDIA_URI: Uri = Uri.parse("content://${MediaStore.AUTHORITY}/")
        val EXTERNAL_PATH_SEGMENTS: MutableList<String> =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.pathSegments
        val PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
    }
}