package com.isanechek.imagehandler.data.local.system.gallery.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.isanechek.imagehandler.data.local.system.AssetsManager
import com.isanechek.imagehandler.utils.TrackerUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class AssetsManagerImpl(private val trackerUtils: TrackerUtils) : AssetsManager {

    override suspend fun loadImagesFromAssets(
        context: Context,
        folder: String,
        imageQuality: Int
    ): List<String> = suspendCancellableCoroutine { coroutine ->
        try {
            val assetsFolderName = "overlay"
            val data = context.assets.list(assetsFolderName)
                ?.toList()
                ?.filterNotNull()
                ?.filter { it.contains("vova_", ignoreCase = true) }
                ?.toList()
                ?: emptyList()

            val temp = mutableListOf<String>()
            if (data.isNotEmpty()) {
                data.forEach { fileName ->
                    val bitmap = context.assets.open("$assetsFolderName/$fileName").use {
                        BitmapFactory.decodeStream(it)
                    }
                    val file = File("${folder}/$fileName")
                    if (file.exists()) {
                        file.delete()
                    }
                    val comp =
                        if (fileName.contains(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    FileOutputStream(file).use {
                        bitmap.compress(comp, 100, it)
                    }
                    temp.add(file.absolutePath)
                }
            }
            coroutine.resume(temp)
        } catch (e: Exception) {

        }
    }

}