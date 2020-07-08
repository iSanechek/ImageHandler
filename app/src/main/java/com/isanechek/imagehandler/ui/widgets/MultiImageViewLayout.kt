package com.isanechek.imagehandler.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import coil.api.load
import com.isanechek.imagehandler.R
import com.isanechek.imagehandler.debugLog
import java.io.File


class MultiImageViewLayout : LinearLayout {
    private var mContext: Context
    private var mImageViewcache: ImageView? = null

    // 照片的Url列表
    private var imagesList: List<String>? = null
    private val mImageViews: MutableList<ImageView?> = ArrayList<ImageView?>()

    /**
     * 长度 单位为Pixel
     */
    private var pxOneMaxWandH // 单张图最大允许宽高
            = 0
    private var pxMoreWandH = 0 // 多张图的宽高
    private val pxImagePadding = dip2px(context, 3f) // 图片间的间距
    private var MAX_PER_ROW_COUNT = 3 // 每行显示最大数
    private var onePicPara: LayoutParams? = null
    private var morePara: LayoutParams? = null
    private var moreParaColumnFirst: LayoutParams? = null
    private var rowPara: LayoutParams? = null
    private var mOnItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
    }

    @Throws(IllegalArgumentException::class)
    fun setList(lists: List<String>?) {
        requireNotNull(lists) { "imageList is null..." }
        imagesList = lists
        if (MAX_WIDTH > 0) {
            pxMoreWandH =
                (MAX_WIDTH - pxImagePadding * 2) / 3 //解决右侧图片和内容对不齐问题
            pxOneMaxWandH = MAX_WIDTH * 2 / 3
            initImageLayoutParams()
        }
        initView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MAX_WIDTH == 0) {
            val width = measureWidth(widthMeasureSpec)
            if (width > 0) {
                MAX_WIDTH = width
                if (imagesList != null && imagesList!!.size > 0) {
                    setList(imagesList)
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private fun measureWidth(measureSpec: Int): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize
        } else {
            // Measure the text
            // result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
            // + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    private fun initImageLayoutParams() {
        onePicPara =
            LayoutParams(pxOneMaxWandH, LayoutParams.WRAP_CONTENT)
        moreParaColumnFirst = LayoutParams(pxMoreWandH, pxMoreWandH)
        morePara = LayoutParams(pxMoreWandH, pxMoreWandH)
        morePara!!.setMargins(pxImagePadding, 0, 0, 0)
        rowPara = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    // 根据imageView的数量初始化不同的View布局,还要为每一个View作点击效果
    private fun initView() {
        this.orientation = VERTICAL
        RemoveAllChild(this)
        if (MAX_WIDTH == 0) {
            //为了触发onMeasure()来测量MultiImageView的最大宽度，MultiImageView的宽设置为match_parent
            addView(View(context))
            return
        }
        if (imagesList == null || imagesList!!.size == 0) {
            return
        }
        if (imagesList!!.size == 1) {
            addView(getImageViewFromCache(0, false))
        } else {
            val allCount = imagesList!!.size
            MAX_PER_ROW_COUNT = if (allCount == 4) {
                2
            } else {
                3
            }
            val rowCount =
                allCount / MAX_PER_ROW_COUNT + if (allCount % MAX_PER_ROW_COUNT > 0) 1 else 0 // 行数
            for (rowCursor in 0 until rowCount) {
                val rowLayout = LinearLayout(context)
                rowLayout.orientation = HORIZONTAL
                rowLayout.layoutParams = rowPara
                if (rowCursor != 0) {
                    rowLayout.setPadding(0, pxImagePadding, 0, 0)
                }
                var columnCount =
                    if (allCount % MAX_PER_ROW_COUNT == 0) MAX_PER_ROW_COUNT else allCount % MAX_PER_ROW_COUNT //每行的列数
                if (rowCursor != rowCount - 1) {
                    columnCount = MAX_PER_ROW_COUNT
                }
                addView(rowLayout)
                val rowOffset = rowCursor * MAX_PER_ROW_COUNT // 行偏移
                for (columnCursor in 0 until columnCount) {
                    val position = columnCursor + rowOffset
                    rowLayout.addView(getImageViewFromCache(position, true))
                }
            }
        }
    }

    private fun RemoveAllChild(mViewGroup: ViewGroup) {
        for (i in 0 until mViewGroup.childCount) {
            if (getChildAt(i) is ViewGroup) {
                (getChildAt(i) as ViewGroup).removeAllViews()
            }
        }
        removeAllViews()
    }

    private fun getImageViewFromCache(position: Int, isMultiImage: Boolean): ImageView? {
        var mImageView: ImageView? = null
        if (!isMultiImage) {
            if (mImageViewcache == null) {
                mImageViewcache = ImageView(context)
            }
            mImageViewcache?.adjustViewBounds = true
            mImageViewcache?.scaleType = ScaleType.FIT_START
            mImageViewcache?.maxHeight = pxOneMaxWandH
            mImageViewcache?.layoutParams = onePicPara
            mImageView = mImageViewcache
        } else {
            for (i in mImageViews.indices) {
                if (mImageViews[i]?.parent == null) {
                    mImageView = mImageViews[i]
                    break
                }
            }
            if (mImageView == null) {
                mImageView = ImageView(context)
                mImageView.scaleType = ScaleType.CENTER_CROP
                mImageView.layoutParams = if (position % MAX_PER_ROW_COUNT == 0) moreParaColumnFirst else morePara
                mImageViews.add(mImageView)
            }
        }
        val PressX = FloatArray(1)
        val PressY = FloatArray(1)
        val url = imagesList!![position]
        mImageView?.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    (v as ImageView).setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                    PressX[0] = event.x
                    PressY[0] = event.y
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> (v as ImageView).setColorFilter(
                    Color.TRANSPARENT
                )
                else -> {
                }
            }
            false
        })
        mImageView?.setOnClickListener(OnClickListener { v ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(
                    v,
                    v.getTag(R.id.FriendLife_Position) as Int,
                    PressX[0],
                    PressY[0]
                )
            }
        })
        mImageView?.setOnLongClickListener(OnLongClickListener { v ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemLongClick(
                    v,
                    v.getTag(R.id.FriendLife_Position) as Int,
                    PressX[0],
                    PressY[0]
                )
            }
            true
        })
        mImageView?.id = url.hashCode()
        mImageView?.setTag(R.id.FriendLife_Position, position)
        setImageGlide(mContext, url, mImageView)
        //        ImageLoader.getInstance().displayImage(url, mImageView);
        return mImageView
    }

    interface OnItemClickListener {
        fun onItemClick(
            view: View?,
            PressImagePosition: Int,
            PressX: Float,
            PressY: Float
        )

        fun onItemLongClick(
            view: View?,
            PressImagePosition: Int,
            PressX: Float,
            PressY: Float
        )
    }

    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * glide加载图片
     */
//    private var glideRequest: RequestManager? = null
    private fun setImageGlide(
        mContext: Context,
        PicURL: String,
        mImageView: ImageView?
    ) {
        debugLog { "PIC URL $PicURL" }
        mImageView?.load(File(PicURL))
//        glideRequest = Glide.with(mContext)
//        glideRequest.load(PicURL).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate()
//            .error(R.mipmap.ic_launcher).into(mImageView)
    }

    companion object {
        var MAX_WIDTH = 0
    }
}