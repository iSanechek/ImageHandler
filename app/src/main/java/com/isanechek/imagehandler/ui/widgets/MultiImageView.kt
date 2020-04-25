/*******************************************************************************
 * Copyright 2016 stfalcon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.isanechek.imagehandler.ui.widgets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import java.util.*

/**
 * Created by Anton Bevza on 12/22/16.
 */

class MultiImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    //Shape of view
    var shape = Shape.NONE
        set(value) {
            field = value
            invalidate()
        }
    //Corners radius for rectangle shape
    var rectCorners = 100

    private val bitmaps = ArrayList<Bitmap>()
    private val path = Path()
    private val rect = RectF()
    private var multiDrawable: Drawable? = null

    /**
     * Add image to view
     */
    fun addImage(bitmap: Bitmap) {
        bitmaps.add(bitmap)
        refresh()
    }

    /**
     * Remove all images
     */
    fun clear() {
        bitmaps.clear()
        refresh()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refresh()
    }

    /**
     * recreate MultiDrawable and set it as Drawable to ImageView
     */
    private fun refresh() {
        multiDrawable = MultiDrawable(bitmaps)
        setImageDrawable(multiDrawable)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            if (drawable != null) {
                //if shape not set - just draw
                if (shape != Shape.NONE) {
                    path.reset()
                    //ImageView size
                    rect.set(0f, 0f, width.toFloat(), height.toFloat())
                    if (shape == Shape.RECTANGLE) {
                        //Rectangle with corners
                        path.addRoundRect(rect, rectCorners.toFloat(),
                            rectCorners.toFloat(), Path.Direction.CW)
                    } else {
                        //Oval
                        path.addOval(rect, Path.Direction.CW)
                    }
                    //Clip with shape
                    canvas.clipPath(path)
                }
                super.onDraw(canvas)
            }
        }
    }

    //Types of shape
    enum class Shape {
        CIRCLE, RECTANGLE, NONE
    }
}

