package com.isanechek.imagehandler.ui.handler.preview

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.Image
import com.isanechek.imagehandler.databinding.SelectPreviewScreenLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui.handler.choices.SelectImgViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectPreviewScreen : BaseFragment(_layout.select_preview_screen_layout) {

    private val binding by viewBinding(SelectPreviewScreenLayoutBinding::bind)
    private val vm: SelectImgViewModel by sharedViewModel()

    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            binding.spsToolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        binding.spsToolbar.apply {
            title = "Превью"
            setBackOrCloseButton { findNavController().navigateUp() }
        }
        vm.selected.observe(this, Observer(::bindList))

        binding.spsStartBtn.onClick {
            findNavController().navigate(_id.go_to_handler_from_select_preview)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.spsList.addOnScrollListener(recyclerScrollListener)
    }

    override fun onPause() {
        binding.spsList.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vm.clearCache()
    }

    private fun bindList(data: List<Image>) {
        with(binding.spsList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SelectPreviewAdapter(data) { item -> }
        }
    }
}