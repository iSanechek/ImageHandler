package com.isanechek.imagehandler.ui.handler.preview

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui.base.FastListAdapter
import com.isanechek.imagehandler.ui.base.bind
import com.isanechek.imagehandler.ui.handler.choices.SelectImgViewModel
import kotlinx.android.synthetic.main.select_preview_item_layout.view.*
import kotlinx.android.synthetic.main.select_preview_screen_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

class SelectPreviewScreen : BaseFragment(_layout.select_preview_screen_layout) {

    private val vm: SelectImgViewModel by sharedViewModel()

    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            sps_toolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        sps_toolbar.apply {
            title = "Превью"
            setBackOrCloseButton { findNavController().navigateUp() }
        }
        vm.selected.observe(this, Observer(::bindList))

        sps_start_btn.onClick {
            findNavController().navigate(_id.go_to_handler_from_select_preview)
        }
    }

    override fun onStart() {
        super.onStart()
        sps_list.addOnScrollListener(recyclerScrollListener)
    }

    override fun onPause() {
        sps_list.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vm.clearCache()
    }

    private fun bindList(data: List<Image>) {
        sps_list.apply {
            bind(
                items = data,
                singleLayout = _layout.select_preview_item_layout
            ) { item: Image, _: Int ->
                spi_cover.load(File(item.path))

            }

        }
    }

}