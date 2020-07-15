package com.isanechek.imagehandler.data.local.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.isanechek.imagehandler.debugLog
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

interface FilesManager {
    fun getFileName(path: String): String
    fun createFolderIfEmpty(path: String): Boolean
    fun copyFile(originPath: String, copyPath: String, copyFileName: String): Pair<Boolean, String>
    fun checkFileExists(path: String): Boolean
    fun saveFile(bitmap: Bitmap, folderPath: String, fileName: String): Pair<Boolean, String>
    fun clearAll(path: String): Boolean
    fun deleteFile(path: String): Boolean
    fun replaceBitmap(name: String, path: String, bitmap: Bitmap): Boolean
    fun checkFolderExistsAndIsNotEmpty(path: String): Boolean
    suspend fun loadImagesFromAssets(context: Context): List<String>
}

class FilesManagerImpl : FilesManager {

    // Здесь надо впилить проверку файла
    override fun getFileName(path: String): String = File(path).nameWithoutExtension

    override fun createFolderIfEmpty(path: String): Boolean {
        var isCreated = true
        val folder = File(path)
        if (!folder.exists()) {
            isCreated = folder.mkdirs()
        }
        return isCreated
    }

    override fun copyFile(
        originPath: String,
        copyPath: String,
        copyFileName: String
    ): Pair<Boolean, String> {
        var result = Pair(false, "empty")
        val copyFile =
            File(originPath).copyTo(File(copyPath, "copy_$copyFileName.jpg"), overwrite = true)
        if (copyFile.exists() && copyFile.length() > 0) {
            result = Pair(first = true, second = copyFile.absolutePath)
        }
        return result
    }

    override fun checkFileExists(path: String): Boolean = File(path).exists()

    override fun saveFile(
        bitmap: Bitmap,
        folderPath: String,
        fileName: String
    ): Pair<Boolean, String> {
        var result = Pair(false, "")
        val fileResult = File(folderPath, fileName)
        FileOutputStream(fileResult).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        if (fileResult.exists() && fileResult.length() > 0) {
            result = Pair(true, fileResult.absolutePath)
        }
        return result
    }

    override fun clearAll(path: String): Boolean {
        var status = true
        val folder = File(path)
        if (folder.isDirectory) {
            folder.listFiles()?.forEach {
                if (!it.delete()) {
                    status = false
                }
            }
        }
        folder.listFiles()?.forEach { file ->
            debugLog { "FILE NOT REMOVING ${file.name}" }
        }
        return status
    }

    override fun deleteFile(path: String): Boolean {
        debugLog { "DELETE FILE PAT $path" }
        var isDeleted = false
        val file = File(path)
        if (file.isFile) {
            debugLog { "FILE ${file.name} DELETE" }
            isDeleted = when {
                file.exists() -> {
                    file.deleteOnExit()
                    !file.exists()
                }
                else -> true
            }
        } else {
            debugLog { "IS NOT FILE" }
        }
        debugLog { "Path $path status $isDeleted" }
        return isDeleted
    }

    override fun replaceBitmap(name: String, path: String, bitmap: Bitmap): Boolean {
        debugLog { "PATH $path" }
        debugLog { "NAME $name" }


        return true
    }

    override fun checkFolderExistsAndIsNotEmpty(path: String): Boolean {
        val folder = File(path)
        if (!folder.exists()) return false

        if (folder.isDirectory) {
            return folder.listFiles()?.isNotEmpty() ?: false
        }

        return false
    }

    override suspend fun loadImagesFromAssets(context: Context): List<String> =
        suspendCancellableCoroutine { c ->
            try {
                val assetsFolderName = "overlay"
                val data = context.assets.list(assetsFolderName)
                    ?.toList()
                    ?.filterNotNull()
                    ?.filter { it.contains("vova_", ignoreCase = true) }
                    ?.toList()
                    ?: emptyList()

                if (data.isEmpty()) {
                    c.resume(data)
                } else {
                    val temp = mutableListOf<String>()
                    val cachePath = context.filesDir.absolutePath + File.separator + "overlay_images"
                    if (createFolderIfEmpty(cachePath)) {
                        data.forEach { fileName ->
                            val bitmap = context.assets.open("$assetsFolderName/$fileName").use {
                                BitmapFactory.decodeStream(it)
                            }
                            val file = File("${cachePath}/$fileName")
                            if (file.exists()) {
                                file.delete()
                            }
                            val comp = if (fileName.contains(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                            FileOutputStream(file).use {
                                bitmap.compress(comp, 100, it)
                            }
                            temp.add(file.absolutePath)
                        }
                    }

                    c.resume(temp)
                }

            } catch (ex: Exception) {
                debugLog { "Load Images From Assets Error! ${ex.message}" }
                c.resume(emptyList())
            }
        }

}