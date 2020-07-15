package com.isanechek.imagehandler.ui.crop

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.isanechek.imagehandler._drawable
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.crop_sceen_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CropScreen : BaseFragment(_layout.crop_sceen_layout) {

    private val vm: CropViewModel by viewModel()

    private var isCrop = true

    private val id: String
        get() = arguments?.getString("crop_id", "") ?: ""

    private val originalPath: String
        get() = arguments?.getString("crop_origin_path", "" ) ?: ""


    private var currentItem: ImageItem? = null
    override fun bindUi(savedInstanceState: Bundle?) {
        debugLog { "CROP ID $id" }
        debugLog { "CROP ORIGIN PATH $originalPath" }

        csl_close_btn.onClick { findNavController().navigateUp() }
        setupMagicBtn()

        vm.loadItem(id).observe(this, Observer { item ->
            if (item != null) {
                setupCrop(item)
                currentItem = item
            } else debugLog { "CROP ITEM NULL" }
        })

        vm.rBitmap.observe(this, Observer { bitmap ->
            csl_cover.load(bitmap)
        })

        csl_crop_btn.onClick {
            if (isCrop) {
                csl_cropper.getCroppedImageAsync()
            } else {
                if (currentItem != null) {

                }
            }
        }

    }

    private fun setupMagicBtn() {
        if (isCrop) {
            csl_magic_btn.apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), _drawable.ic_baseline_compare_24))
                onClick {
                    isCrop = false
                    csl_cropper.isVisible = false
                    csl_cover.isVisible = true
                    vm.overlayBitmap(originalPath)
                    setupMagicBtn()
                }
            }
        } else {
            csl_magic_btn.apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), _drawable.ic_baseline_transform_24))
                onClick {
                    isCrop = true
                    csl_cover.isVisible = false
                    csl_cropper.isVisible = true
                    setupMagicBtn()
                }
            }
        }
    }

    private fun setupCrop(item: ImageItem) {
        csl_cropper.apply {
            setImageBitmap(BitmapFactory.decodeFile(item.originalPath))
            setAspectRatio(1, 1)
            setFixedAspectRatio(true)
            setOnCropImageCompleteListener { _, result ->
                if (result.isSuccessful) {
                    vm.updateItem(item, result.bitmap)
                }
            }
        }
    }

}