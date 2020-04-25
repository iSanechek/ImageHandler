package com.isanechek.imagehandler.ui.widgets

import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import java.util.ArrayList

class MultiDrawable(private val bitmaps: ArrayList<Bitmap>) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val items = ArrayList<PhotoItem>()

    /**
     * Create PhotoItem with position and size depends of count of images
     */
    private fun init() {
        items.clear()
        when (bitmaps.size) {
            1 -> {
                val bitmap = scaleCenterCrop(bitmaps[0], bounds.width(), bounds.height())
                items.add(PhotoItem(bitmap, Rect(0, 0, bounds.width(), bounds.height())))
            }
            2 -> {
                val bitmap1 = scaleCenterCrop(bitmaps[0], bounds.width(), bounds.height() / 2)
                val bitmap2 = scaleCenterCrop(bitmaps[1], bounds.width(), bounds.height() / 2)
                items.add(PhotoItem(bitmap1, Rect(0, 0, bounds.width() / 2, bounds.height())))
                items.add(PhotoItem(bitmap2, Rect(bounds.width() / 2, 0, bounds.width(), bounds.height())))
            }
            3 -> {
                val bitmap1 = scaleCenterCrop(bitmaps[0], bounds.width(), bounds.height() / 2)
                val bitmap2 = scaleCenterCrop(bitmaps[1], bounds.width() / 2, bounds.height() / 2)
                val bitmap3 = scaleCenterCrop(bitmaps[2], bounds.width() / 2, bounds.height() / 2)
                items.add(PhotoItem(bitmap1, Rect(0, 0, bounds.width() / 2, bounds.height())))
                items.add(PhotoItem(bitmap2, Rect(bounds.width() / 2, 0, bounds.width(), bounds.height() / 2)))
                items.add(PhotoItem(bitmap3, Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())))
            }
        }
        if (bitmaps.size == 4) {
            val bitmap1 = scaleCenterCrop(bitmaps[0], bounds.width() / 2, bounds.height() / 2)
            val bitmap2 = scaleCenterCrop(bitmaps[1], bounds.width() / 2, bounds.height() / 2)
            val bitmap3 = scaleCenterCrop(bitmaps[2], bounds.width() / 2, bounds.height() / 2)
            val bitmap4 = scaleCenterCrop(bitmaps[3], bounds.width() / 2, bounds.height() / 2)
            items.add(PhotoItem(bitmap1, Rect(0, 0, bounds.width() / 2, bounds.height() / 2)))
            items.add(PhotoItem(bitmap2, Rect(0, bounds.height() / 2, bounds.width() / 2, bounds.height())))
            items.add(PhotoItem(bitmap3, Rect(bounds.width() / 2, 0, bounds.width(), bounds.height() / 2)))
            items.add(PhotoItem(bitmap4, Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())))
        }
    }

    override fun draw(canvas: Canvas) {
        items.forEach {
            canvas.drawBitmap(it.bitmap, bounds, it.position, paint)
        }
    }

    /**
     * scale and center crop image
     */
    private fun scaleCenterCrop(source: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(source, newWidth, newHeight)
    }

    /***
     * Data class for store bitmap and position
     */
    data class PhotoItem(val bitmap: Bitmap, val position: Rect)


    //***Needed to override***//
    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        init()
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
    //***------------------***//
}