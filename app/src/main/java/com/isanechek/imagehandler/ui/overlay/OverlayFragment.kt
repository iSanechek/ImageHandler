package com.isanechek.imagehandler.ui.overlay

import android.content.res.Resources
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.GalleryImageResult
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui.overlay.adapters.OverlayChoicesDialogAdapter
import com.isanechek.imagehandler.ui.widgets.CustomBottomSheetBehavior
import kotlinx.android.synthetic.main.overlay_fragment_layout.*
import kotlinx.android.synthetic.main.watermark_choices_dialog_custom_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class OverlayFragment : BaseFragment(_layout.overlay_fragment_layout) {

    private val vm: OverlayViewModel by viewModel()
    private val bottomSheetBehavior: CustomBottomSheetBehavior<View> = CustomBottomSheetBehavior(dpToPx(100))
    private val listScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            when {
                dy > 0 && cf_fab.isVisible -> cf_fab.hide()
                dy < 0 && !cf_fab.isVisible -> cf_fab.show()
            }
            cf_toolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        cf_toolbar.apply {
            hideCustomLayout()
        }

        cf_fab.onClick {
            debugLog { "boom" }

//            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
//                bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED
//            }

            findNavController().navigate(_id.go_from_overlay_choices)


        }

//        setupBehavior()
        setupDescription()
        setupList()
    }

    override fun onResume() {
        super.onResume()
        cf_list.addOnScrollListener(listScrollListener)
    }

    override fun onPause() {
        cf_list.removeOnScrollListener(listScrollListener)
        super.onPause()
    }

    private fun setupList() {
        with(cf_list) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupDescription() {
        val ss =
            SpannableString("На данный момент нет активных хаданий.\nДля того чтобы добавить задание нажмите \n ").apply {
                setSpan(
                    ImageSpan(
                        requireContext(),
                        _drawable.ic_baseline_add_to_photos_black_24
                    ),
                    this.length.minus(1),
                    this.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

    }

    private fun setupBehavior() {
        bottomSheetBehavior.state = CustomBottomSheetBehavior.STATE_COLLAPSED

        val imagesAdapter = OverlayChoicesDialogAdapter()
        with(test_dialog_list) {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imagesAdapter
        }

        vm.loadImages(true).observe(this, Observer { data ->
            when(data) {
                is GalleryImageResult.Load -> debugLog { "load data" }
                is GalleryImageResult.Done -> {
                    imagesAdapter.submit(data.data)
                }
                is GalleryImageResult.Update -> {
                    imagesAdapter.submit(data.data)
                }
                is GalleryImageResult.Error -> {
                    imagesAdapter.submit(data.data)
                }
            }
        })
    }

    private fun showChoicesDialog() {
        val imagesAdapter = OverlayChoicesDialogAdapter()
        val dialog = MaterialDialog(requireContext(), BottomSheet()).show {
            customView(viewRes = _layout.watermark_choices_dialog_custom_layout)
            lifecycleOwner(this@OverlayFragment)
        }

        dialog.onShow {

            val root = it.getCustomView()
            val list = root.findViewById<RecyclerView>(_id.test_dialog_list)

            with(list) {
                setHasFixedSize(true)
                isNestedScrollingEnabled = true
                setItemViewCacheSize(12)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = imagesAdapter
            }

            vm.loadImages(true).observe(this, Observer { data ->
                when(data) {
                    is GalleryImageResult.Load -> debugLog { "load data" }
                    is GalleryImageResult.Done -> {
                        imagesAdapter.submit(data.data)
                    }
                    is GalleryImageResult.Update -> {
                        imagesAdapter.submit(data.data)
                    }
                    is GalleryImageResult.Error -> {
                        imagesAdapter.submit(data.data)
                    }
                }
            })
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

}