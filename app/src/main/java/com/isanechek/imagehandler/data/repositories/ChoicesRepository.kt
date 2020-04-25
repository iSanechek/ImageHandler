package com.isanechek.imagehandler.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.isanechek.imagehandler.data.local.database.dao.GalleryDao
import com.isanechek.imagehandler.data.local.database.entity.AlbumEntity
import com.isanechek.imagehandler.data.local.database.entity.FolderEntity
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.Album
import com.isanechek.imagehandler.data.models.ChoicesResult
import com.isanechek.imagehandler.data.models.Folder
import com.isanechek.imagehandler.data.models.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ChoicesRepository {
    fun loadFolders(context: Context, isUpdate: Boolean): LiveData<ChoicesResult<List<Album>>>
    fun loadFolders(context: Context): LiveData<ChoicesResult<List<Folder>>>
}

class ChoicesRepositoryImpl(
    private val galleryManager: GalleryManager,
    private val galleryDao: GalleryDao
) : ChoicesRepository {

    private val toFolderModel: (FolderEntity) -> Folder = { f ->
        Folder(
            id = f.id,
            modification = f.modification,
            caverPaths = Folder.stringToCoverPathsList(f.covers),
            name = f.name
        )
    }

    override fun loadFolders(
        context: Context,
        isUpdate: Boolean
    ): LiveData<ChoicesResult<List<Album>>> = liveData(Dispatchers.IO) {
        val cache = galleryDao.loadAlbums()
        if (isUpdate) {
            if (cache.isNotEmpty()) {
                emit(ChoicesResult.Update(cache.map { it.toModel() }))
                val result = galleryManager.loadAlbums(context, 0)
                if (result.isNotEmpty()) {
                    emit(ChoicesResult.Done(result))
                    galleryDao.update(result.map { it.toEntity() })
                } else {
                    galleryDao.clearAlbums()
                    emit(ChoicesResult.Error(emptyList<Album>(), "Folders not find!"))
                }
            } else {
                emit(ChoicesResult.Load)
                val result = galleryManager.loadAlbums(context, 0)
                if (result.isNotEmpty()) {
                    emit(ChoicesResult.Done(result))
                    galleryDao.update(result.map { it.toEntity() })
                } else {
                    galleryDao.clearAlbums()
                    emit(ChoicesResult.Error(emptyList<Album>(), "Folders not find!"))
                }
            }
        } else {
            emit(ChoicesResult.Load)
            if (cache.isNotEmpty()) {
                emit(ChoicesResult.Done(cache.map { it.toModel() }))
            } else {
                val result = galleryManager.loadAlbums(context, 0)
                if (result.isNotEmpty()) {
                    emit(ChoicesResult.Done(result))
                    galleryDao.insertAlbums(result.map { it.toEntity() })
                } else {
                    emit(ChoicesResult.Error(emptyList<Album>(), "Folders not find!"))
                }
            }
        }
    }

    override fun loadFolders(context: Context): LiveData<ChoicesResult<List<Folder>>> =
        liveData(Dispatchers.IO) {
            emit(ChoicesResult.Load)
            val cache = galleryDao.loadFolders()
            if (cache.isNotEmpty()) {
                emit(ChoicesResult.Done(cache.map { it.toModel() }))
            } else {
                val result = galleryManager.loadFolders(context)
                if (result.isNotEmpty()) {
                    emit(ChoicesResult.Done(result))
                    galleryDao.insertFolders(result.map { it.toEntity() })
                } else {
                    emit(ChoicesResult.Error(emptyList<Folder>(), "Not find folders!"))
                }
            }
        }

    private fun FolderEntity.toModel(): Folder = Folder(
        id = this.id,
        name = this.name,
        modification = this.modification,
        caverPaths = Folder.stringToCoverPathsList(this.covers)
    )

    private fun Folder.toEntity(): FolderEntity = FolderEntity(
        id = this.id,
        modification = this.modification,
        name = this.name,
        covers = Folder.coverPathsToString(this.caverPaths)
    )

    private fun Album.toEntity(): AlbumEntity {
        return AlbumEntity(
            id = this.id,
            title = this.name,
            coverPath = this.images.first().path,
            lastModification = this.lastModification,
            path = this.path
        )
    }

    private fun AlbumEntity.toModel(): Album = Album(
        id = this.id,
        lastModification = this.lastModification,
        name = this.title,
        addDate = 0,
        path = this.path,
        images = mutableListOf(Image(0, this.path, "", 0, ""))
    )

}