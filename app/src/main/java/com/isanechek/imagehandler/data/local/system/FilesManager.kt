package com.isanechek.imagehandler.data.local.system

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.isanechek.imagehandler.d
import kotlinx.coroutines.suspendAtomicCancellableCoroutine
import java.io.File
import java.io.FileOutputStream

interface FilesManager {
    fun getFileName(path: String): String
    fun createFolderIfEmpty(path: String): Boolean
    fun copyFile(originPath: String, copyPath: String, copyFileName: String): Pair<Boolean, String>
    fun checkFileExists(path: String): Boolean
    fun saveFile(bitmap: Bitmap, folderPath: String, fileName: String): Pair<Boolean, String>
    fun clearAll(path: String): Boolean
    fun deleteFile(path: String): Boolean
}

class FilesManagerImpl : FilesManager {

    // Здесь надо впилить проверку файла
    override fun getFileName(path: String): String = File(path).nameWithoutExtension

    override fun createFolderIfEmpty(path: String): Boolean {
        var isCreated = true
        val folder = File(path)
        if (!folder.exists()) { isCreated = folder.mkdirs() }
        return isCreated
    }

    override fun copyFile(originPath: String, copyPath: String, copyFileName: String): Pair<Boolean, String> {
        var result = Pair(false, "empty")
        val copyFile = File(originPath).copyTo(File(copyPath, "copy_$copyFileName.jpg"), overwrite = true)
        if (copyFile.exists() && copyFile.length() > 0) {
            result = Pair(first = true, second = copyFile.absolutePath)
        }
        return result
    }

    override fun checkFileExists(path: String): Boolean = File(path).exists()

    override fun saveFile(bitmap: Bitmap, folderPath: String, fileName: String): Pair<Boolean, String> {
        var result = Pair(false, "")
        val bit = BitmapFactory.decodeFile("")
        val fileResult = File(folderPath, "result_$fileName.jpg")
        FileOutputStream(fileResult).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        if (fileResult.exists() && fileResult.length() > 0) {
            result = Pair(true, fileResult.absolutePath)
        }
        return result
    }

    override fun clearAll(path: String): Boolean {
        var result = true
        val folder = File(path)
        if (folder.isDirectory) {
            folder.listFiles()?.forEach {
                it.delete()
            }
        }
        return result
    }

    override fun deleteFile(path: String): Boolean {
        var isDeleted = false
        val file = File(path)
        if (file.isFile) { isDeleted = file.delete() }
        d { "Path $path status $isDeleted" }
        return isDeleted
    }

}