package com.isanechek.imagehandler.ui.overlay

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.overlay_screen_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class OverlayFragment : BaseFragment(_layout.overlay_screen_layout) {

    private val vm: OverlayViewModel by viewModel()

    @SuppressLint("SetTextI18n")
    override fun bindUi(savedInstanceState: Bundle?) {
        osl_toolbar.hideCustomLayout()
        osl_toolbar_title.text = "ImageHandler"

//        with(osl_toolbar_overlay) {
//            onClick {
//                debugLog { "BOOM" }
//                findNavController().navigate(_id.overlay_go_watermark)
//            }
//            setBorderColor(ContextCompat.getColor(requireContext(), _color.colorBlack))
//            setBorderWidth(2)
//        }
        vm.overlayBitmap().observe(this, Observer { bitmap ->
            if (bitmap != null) {
                osl_toolbar_overlay_add.apply { if (isVisible) isVisible = false }
                osl_toolbar_overlay.apply {
                    if (isGone) isGone = false
                    setImageBitmap(bitmap)
                    setOnClickListener {
//                        findNavController().navigate(_id.overlay_go_watermark)
                    }
                }
            } else {
                osl_toolbar_overlay.apply { if (isVisible) isVisible = false }
                osl_toolbar_overlay_add.apply {
                    if (isGone) isGone = false
                    onClick {
//                        findNavController().navigate(_id.overlay_go_watermark)
                    }
                }
            }
        })
    }

}