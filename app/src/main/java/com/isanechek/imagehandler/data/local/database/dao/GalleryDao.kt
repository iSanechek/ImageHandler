package com.isanechek.imagehandler.data.local.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.isanechek.imagehandler.data.local.database.entity.AlbumEntity
import com.isanechek.imagehandler.data.local.database.entity.FolderEntity
import com.isanechek.imagehandler.data.local.database.entity.GalleryImage
import com.isanechek.imagehandler.data.models.Image

@Dao
interface GalleryDao {

    @Query("SELECT * FROM album ORDER BY last_modification DESC")
    suspend fun loadAlbums(): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(data: List<AlbumEntity>)

    @Query("DELETE FROM album")
    suspend fun clearAlbums()

    @Transaction
    suspend fun update(newData: List<AlbumEntity>) {
        clearAlbums()
        insertAlbums(newData)
    }

    /*Folders*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(items: List<FolderEntity>)

    @Query("SELECT * FROM folders ORDER BY modification DESC" )
    suspend fun loadFolders(): List<FolderEntity>

    @Query("DELETE FROM folders")
    suspend fun clearFolders()

    @Transaction
    suspend fun updateFolders(data: List<FolderEntity>) {
        clearFolders()
        insertFolders(data)
    }

    /*Images*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(data: List<GalleryImage>)

    @Query("SELECT * FROM gallery_images ORDER BY add_time DESC")
    suspend fun loadImages(): List<GalleryImage>

    @Query("SELECT id FROM gallery_images")
    suspend fun loadImagesIds(): List<Long>

    @Query("DELETE FROM gallery_images")
    suspend fun clearImages()

    @Query("SELECT * FROM gallery_images WHERE id =:id")
    suspend fun loadImage(id: Long): GalleryImage?

    @Query("DELETE FROM gallery_images WHERE id =(:ids)")
    suspend fun removeImages(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM gallery_images")
    suspend fun getImagesSize(): Int

    @Transaction
    suspend fun updateImages(data: List<GalleryImage>) {
        clearImages()
        insertImages(data)
    }

    @Query("SELECT * FROM gallery_images WHERE folder_name =:albumNAme ORDER BY add_time DESC")
    suspend fun loadImagesFromAlbum(albumNAme: String): List<GalleryImage>
}