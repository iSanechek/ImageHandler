package com.isanechek.imagehandler.data.local.system.gallery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.models.*
import com.isanechek.imagehandler.doWhile
import com.isanechek.imagehandler.replace
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface GalleryManager {
    suspend fun loadImages(context: Context, request: GalleryRequest): Flow<GalleryImageResult>
    suspend fun loadLastImages(
        context: Context,
        folderName: String,
        loadSize: Int,
        offset: Int
    ): List<Image>

    suspend fun loadAlbums(context: Context, offset: Int): List<Album>
    suspend fun loadFolders(context: Context): List<Folder>
}

class GalleryManagerImpl : GalleryManager {

    @ExperimentalCoroutinesApi
    override suspend fun loadImages(
        context: Context,
        request: GalleryRequest
    ): Flow<GalleryImageResult> = callbackFlow {

    }

    override suspend fun loadLastImages(
        context: Context,
        folderName: String,
        loadSize: Int,
        offset: Int
    ): List<Image> =
        suspendCancellableCoroutine { c ->
            try {

                val selection: String? =
                    if (folderName.isEmpty()) null else "${MediaStore.Images.Media.DATA} LIKE ?"
                val args: Array<String>? =
                    if (folderName.isEmpty()) null else arrayOf("%${folderName}%")
                val sort: String? =
                    if (folderName.isNotEmpty()) null else "$ORDER_BY LIMIT $loadSize OFFSET $offset"

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED
                )
                val temp = mutableListOf<Image>()
                val contentResolver = context.contentResolver
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    args,
                    sort
                )?.use { cursor ->
                    cursor.doWhile {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val date =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                        val uri =
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        val realPath = FileUtils.getPath(context, uri)
                        val folderName = realPath.substring(0, realPath.lastIndexOf("/"))
                            .replaceBeforeLast("/", "")
                            .replace("/", "")
                            .replaceAfter(" ", "")
                            .trim()
                        temp.add(
                            Image(
                                id = id,
                                path = realPath,
                                addDate = date,
                                name = name,
                                folderName = folderName
                            )
                        )
                    }
                }

                debugLog { "temp ${temp.size}" }

                c.resume(temp)
            } catch (ex: Throwable) {
                debugLog { "Load images ${ex.message}" }
                c.resume(emptyList())
            }
        }

    override suspend fun loadAlbums(context: Context, offset: Int): List<Album> =
        suspendCancellableCoroutine { c ->
            try {
                val temp = mutableListOf<Album>()
                val contentResolver = context.contentResolver
                val projection = arrayOf(
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.Media.DATE_ADDED
                )
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    cursor.doWhile {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val date =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))

                        val uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val realPath = FileUtils.getPath(context, uri)
                        val folderPath = realPath.substring(0, realPath.lastIndexOf("/"))
                        val folderName = folderPath.replaceBeforeLast("/", "")
                            .replace("/", "")
                            .replaceAfter(" ", "")
                            .trim()


//                        d { "name $name" }
//                        d { "path $realPath" }
//                        d { "folder path $folderPath" }
//                        d { "folder name $folderName" }

                        if (temp.isNotEmpty()) {
                            val item = temp.firstOrNull { item -> item.name == folderName }
                            if (item != null) {
                                if (item.images.size < 4) {
                                    item.images.add(Image(0, realPath, "", 0, ""))
                                    temp.replace(item) { i -> i.id == id }
                                }
                            } else {
                                temp.add(
                                    Album(
                                        id = id,
                                        path = folderPath,
                                        name = folderName,
                                        addDate = date,
                                        lastModification = 0,
                                        images = mutableListOf(Image(0, realPath, "", 0, ""))
                                    )
                                )
                            }
                        } else {
                            temp.add(
                                Album(
                                    id = id,
                                    path = folderPath,
                                    name = folderName,
                                    addDate = date,
                                    lastModification = 0,
                                    images = mutableListOf(Image(0, realPath, "", 0, ""))
                                )
                            )
                        }
                    }
                }

                c.resume(temp)
            } catch (ex: Throwable) {
                debugLog { "Load folder error ${ex.message}" }
                c.resume(emptyList())
            }
        }

    override suspend fun loadFolders(context: Context): List<Folder> {
        val folders = mutableListOf<Folder>()
//        folders.add(Folder(id = "0", name = "All", caverPaths = "")) // нужен для спинера
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            ORDER_BY
        )?.use { cursor ->
            cursor.doWhile {
                val bucketId =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID))
                val modified =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))
                val name =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                        ?: bucketId

                if (!folders.any { f -> f.id == bucketId }) {
                    val paths = loadFolderCovers(context, bucketId)
                    if (paths.isNotEmpty()) {
                        folders.add(Folder(id = bucketId, name = name, modification = modified, caverPaths = paths))
                    }
                }
            }
        }

        return folders
    }

    private fun loadFolderCovers(context: Context, folderId: String): Set<String> {
        val paths = mutableSetOf<String>()
        val limit = 4
        val offset = 0
        val contentResolver = context.contentResolver
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.ImageColumns.BUCKET_ID} =?",
            arrayOf(folderId),
            "$ORDER_BY LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            cursor.doWhile {
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val uri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                val realPath = FileUtils.getPath(context, uri)
                paths.add(realPath)
            }
        }
        return paths
    }


    companion object {
        private const val ORDER_BY = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
    }

}