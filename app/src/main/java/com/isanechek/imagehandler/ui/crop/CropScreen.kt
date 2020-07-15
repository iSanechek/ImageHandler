package com.isanechek.imagehandler.ui.crop

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.data.models.ExecuteResult
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.crop_sceen_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CropScreen : BaseFragment(_layout.crop_sceen_layout) {

    private val id: String
        get() = arguments?.getString("crop_id", "") ?: ""

    private val originalPath: String
        get() = arguments?.getString("crop_origin_path", "" ) ?: ""

    private val vm: CropViewModel by viewModel()
    private var isCrop = true

    override fun bindUi(savedInstanceState: Bundle?) {
        csl_close_btn.onClick { findNavController().navigateUp() }

        setupObserver()
        setupMagicBtn()

        csl_crop_btn.onClick {
            csl_cropper.getCroppedImageAsync()
//            if (isCrop) {
//                csl_cropper.getCroppedImageAsync()
//            } else {
////                if (currentItem != null) {
////
////                }
//            }
        }
    }

    private fun setupObserver() {
        vm.loadItem(id).observe(this, Observer { item ->
            if (item != null) {
                setupCrop(item)
            } else debugLog { "CROP ITEM NULL" }
        })

        vm.rBitmap.observe(this, Observer { bitmap ->
            csl_cover.load(bitmap)
        })

        vm.updateState.observe(this, Observer { status ->
            when(status) {
                is ExecuteResult.Done -> {
                    vm.showToast(status.data)
                    delay(2000) {
                        delay(1500) {
                            vm.hideProgress()
                        }
                        findNavController().navigateUp()
                    }
                }
                is ExecuteResult.Error -> {
                    vm.hideProgress()
                    vm.showToast(status.message)
                }
                is ExecuteResult.Progress -> vm.showProgress()
                else -> Unit
            }
        })

        vm.stateProgress.observe(this, Observer { visible ->
            csl_progress.isInvisible = visible
        })

        vm.stateToast.observe(this, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
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