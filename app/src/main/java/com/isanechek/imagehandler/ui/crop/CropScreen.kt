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
import com.isanechek.imagehandler.databinding.CropSceenLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class CropScreen : BaseFragment(_layout.crop_sceen_layout) {

    private val binding by viewBinding(CropSceenLayoutBinding::bind)

    private val launchType: String
        get() = requireArguments().getString(LAUNCH_TYPE_KEY, DEFAULT_VALUE)

    private val id: String
        get() = requireArguments().getString(CROP_ID_KEY, DEFAULT_VALUE)

    private val originalPath: String
        get() = requireArguments().getString(CROP_PATH_KEY, DEFAULT_VALUE )

    private val vm: CropViewModel by viewModel()
    private var isCrop = true

    override fun bindUi(savedInstanceState: Bundle?) {
        binding.cslCloseBtn.onClick { findNavController().navigateUp() }

        launchMode()


        setupObserver()
        setupMagicBtn()

        binding.cslCropBtn.onClick {
            binding.cslCropper.getCroppedImageAsync()
//            if (isCrop) {
//                csl_cropper.getCroppedImageAsync()
//            } else {
////                if (currentItem != null) {
////
////                }
//            }
        }
    }

    private fun launchMode() {
        when (launchType) {
            EDIT_TYPE_VALUE -> {

            }
            PREVIEW_TYPE_VALUE -> {

            }
            else -> {

            }
        }
    }

    private fun setupObserver() {
        vm.loadItem(id).observe(this, Observer { item ->
            if (item != null) {
                setupCrop(item)
            } else debugLog { "CROP ITEM NULL" }
        })

        vm.rBitmap.observe(this, Observer { bitmap ->
            binding.cslCover.load(bitmap)
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
            binding.cslProgress.isInvisible = visible
        })

        vm.stateToast.observe(this, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupMagicBtn() {
        if (isCrop) {
            binding.cslMagicBtn.apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), _drawable.ic_baseline_compare_24))
                onClick {
                    isCrop = false
                    binding.cslCropper.isVisible = false
                    binding.cslCover.isVisible = true
                    vm.overlayBitmap(originalPath)
                    setupMagicBtn()
                }
            }
        } else {
            binding.cslMagicBtn.apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), _drawable.ic_baseline_transform_24))
                onClick {
                    isCrop = true
                    binding.cslCover.isVisible = false
                    binding.cslCropper.isVisible = true
                    setupMagicBtn()
                }
            }
        }
    }

    private fun setupCrop(item: ImageItem) {
        binding.cslCropper.apply {
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

    companion object {
        const val LAUNCH_TYPE_KEY = "launch.type"
        const val EDIT_TYPE_VALUE = "launch.edit.type"
        const val PREVIEW_TYPE_VALUE = "launch.preview.type"
        const val CROP_ID_KEY = "crop_id"
        const val CROP_PATH_KEY = "crop_origin_path"
        const val DEFAULT_VALUE = ""
    }

}