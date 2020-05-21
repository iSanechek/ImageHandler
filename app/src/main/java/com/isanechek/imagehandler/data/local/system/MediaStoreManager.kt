package com.isanechek.imagehandler.data.local.system

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

interface MediaStoreManager {
    suspend fun saveBitmapToGallery(
        context: Context,
        contentResolver: ContentResolver,
        bitmap: Bitmap,
        fileName: String,
        date: Long
    ): Pair<Boolean, String>

    suspend fun saveBitmapInGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        folderName: String,
        timestamp: Long
    ): Pair<Boolean, String>
}

class MediaStoreManagerImpl : MediaStoreManager {

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun saveBitmapToGallery(
        context: Context,
        contentResolver: ContentResolver,
        bitmap: Bitmap,
        fileName: String,
        date: Long
    ): Pair<Boolean, String> = suspendCancellableCoroutine { c ->

        try {
            if (isQ) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(date)

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.DESCRIPTION, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                    put(MediaStore.Images.Media.DATE_ADDED, date / 1000)
                    when {
                        isQ -> {
                            put(
                                MediaStore.Images.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES + File.separator + "ImageHandler"
                            )
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                        else -> {
                            put(
                                MediaStore.Images.Media.DATA,
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "ImageHandler" + File.separator + "${fileName}.jpg"
                            )
                        }
                    }
                }

                val collection = when {
                    isQ -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val uri = contentResolver.insert(collection, contentValues)
                if (uri != null) {
                    contentResolver.apply {
                        openOutputStream(uri).use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                        }
                        openFileDescriptor(
                            uri,
                            "rw"
                        )?.use {
                            ExifInterface(it.fileDescriptor).apply {
                                setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, timestamp)
                                saveAttributes()
                            }
                        }
                    }

                    if (isQ) {
                        contentValues.apply {
                            clear()
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                            contentResolver.update(uri, contentValues, null, null)
                        }
                    }

                    c.resume(Pair(true, uri.toString()))
                } else {
                    c.resume(Pair(false, "Uri is null!"))
                }
            } else {
                debugLog { "BiBi" }
                val p =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "ImageHandler" + File.separator + "${fileName}.jpg"
                val uriPath = Uri.fromFile(File(p))
                contentResolver.openOutputStream(uriPath)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                    MediaScannerConnection.scanFile(context, arrayOf(p), arrayOf("image/*"), null)
                }

                c.resume(Pair(true, uriPath.toString()))
            }

        } catch (ex: SecurityException) {
            debugLog { "Hyi ${ex.message}" }
            when {
                isQ -> {
                    val e = ex as? RecoverableSecurityException ?: throw ex
                    c.resume(Pair(false, e.message ?: "Save image error!"))
                }
                else -> c.resume(Pair(false, ex.message ?: "Image save error!"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun saveBitmapInGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        folderName: String,
        timestamp: Long
    ): Pair<Boolean, String> = suspendCancellableCoroutine { c->
        try {
            val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(timestamp)
            val contentResolver = context.contentResolver
            if (isQ) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.DESCRIPTION, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                    put(MediaStore.Images.Media.DATE_ADDED, timestamp / 1000)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + folderName
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = contentResolver.insert(collection, contentValues)
                if (uri != null) {
                    contentResolver.apply {
                        openOutputStream(uri).use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        }
                        openFileDescriptor(
                            uri,
                            "rw"
                        )?.use {
                            ExifInterface(it.fileDescriptor).apply {
                                setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, date)
                                saveAttributes()
                            }
                        }
                    }

                    contentValues.apply {
                        clear()
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                        contentResolver.update(uri, contentValues, null, null)
                    }

                    c.resume(Pair(true, FileUtils.getPath(context, uri)!!))
                } else c.resume(Pair(false, "Uri is null!"))

            } else {
                // old version android
                val endPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + folderName + File.separator + fileName
                debugLog { "END PATH $endPath" }
                val uriPath = Uri.fromFile(File(endPath))
                contentResolver.openOutputStream(uriPath)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    MediaScannerConnection.scanFile(context, arrayOf(endPath), arrayOf("image/*"), null)
                }
                c.resume(Pair(true, endPath))
            }
        } catch (ex: SecurityException) {
            when {
                isQ -> {
                    val e = ex as? RecoverableSecurityException ?: throw ex
                    c.resume(Pair(false, e.message ?: "Save image error!"))
                }
                else -> c.resume(Pair(false, ex.message ?: "Image save error!"))
            }
        }
    }

    companion object {
        val isQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

}