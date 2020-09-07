package com.isanechek.imagehandler.ext

import com.isanechek.imagehandler.data.local.database.entity.AlbumEntity
import com.isanechek.imagehandler.data.local.database.entity.CityEntity
import com.isanechek.imagehandler.data.local.database.entity.GalleryImage
import com.isanechek.imagehandler.data.local.database.entity.ImageEntity
import com.isanechek.imagehandler.data.models.Album
import com.isanechek.imagehandler.data.models.City
import com.isanechek.imagehandler.data.models.Image

fun GalleryImage.toImage(): Image = Image(
    id = this.id,
    path = this.path,
    addDate = this.addTime,
    name = this.name,
    folderName = this.folderName
)

fun Image.toEntity(): ImageEntity = ImageEntity(
    id = this.id,
    path = this.path,
    name = this.name,
    addTime = this.addDate
)

fun Image.toGallery(): GalleryImage = GalleryImage(
    id = this.id,
    name = this.name,
    path = this.path,
    addTime = this.addDate,
    folderName = this.folderName
)

fun ImageEntity.toModel(): Image = Image(
    id = this.id,
    name = this.name,
    path = this.path,
    addDate = this.addTime,
    folderName = ""
)

fun AlbumEntity.toModel(): Album = Album(
    id = this.id,
    lastModification = this.lastModification,
    name = this.title,
    addDate = 0,
    path = this.path,
    images = mutableListOf(Image(0, this.path, "", 0, ""))
)

fun Album.toEntity(): AlbumEntity = AlbumEntity(
    id = this.id,
    title = this.name,
    coverPath = this.images.first().path,
    lastModification = this.lastModification,
    path = this.path
)

fun CityEntity.toModel(): City = City(
    id = this.id,
    name = this.name,
    isSelected = this.isSelected,
    overlayPath = this.overlayPath
)

fun City.toEntity(): CityEntity = CityEntity(
    id = this.id,
    name = this.name,
    isSelected = this.isSelected,
    overlayPath = this.overlayPath
)