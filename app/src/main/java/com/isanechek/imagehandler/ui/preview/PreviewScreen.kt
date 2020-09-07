package com.isanechek.imagehandler.ui.preview

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import coil.api.load
import com.isanechek.imagehandler.EMPTY_STRING_VALUE
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.PreviewScreenLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.toFile
import com.isanechek.imagehandler.ui.base.BaseFragment

class PreviewScreen : BaseFragment(_layout.preview_screen_layout) {

    private val binding by viewBinding(PreviewScreenLayoutBinding::bind)

    private val itemId: String
        get() = requireArguments().getString(ITEM_ID_KEY, EMPTY_STRING_VALUE)

    private val previewPath: String
        get() = requireArguments().getString(PATH_PREVIEW_KEY, EMPTY_STRING_VALUE)

    override fun bindUi(savedInstanceState: Bundle?) {

        binding.splCloseBtn.onClick {
            findNavController().navigateUp()
        }

        when {
            itemId.isEmpty() -> {
                showErrorState(PREVIEW_DATA_ERROR_MSG)
            }
            previewPath.isEmpty() -> {
                showErrorState(PREVIEW_DATA_ERROR_MSG)
            }
            else -> {
                showContent()
            }
        }
    }

    private fun showContent() {
        binding.pslCover.load(previewPath.toFile())
    }

    private fun showErrorState(message: String) {

    }

    companion object {
        const val ITEM_ID_KEY = "iik"
        const val PATH_PREVIEW_KEY = "ppk"
        const val PREVIEW_DATA_ERROR_MSG = "Внутренняя ошибка. Невозможно отобразить превью."
    }
}