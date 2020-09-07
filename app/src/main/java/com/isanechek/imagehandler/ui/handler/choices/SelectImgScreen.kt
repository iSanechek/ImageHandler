package com.isanechek.imagehandler.ui.handler.choices

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.SelectImgScreenLayoutBinding
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectImgScreen : BaseFragment(_layout.select_img_screen_layout) {

    private val binding by viewBinding(SelectImgScreenLayoutBinding::bind)
    private val vm: SelectImgViewModel by sharedViewModel()
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var keyProvider: SelectItemKeyProvider
    private val trackerObserverListener = object : SelectionTracker.SelectionObserver<Long>() {

        override fun onSelectionRestored() {
            super.onSelectionRestored()
            debugLog { "Restored ${tracker.selection.size()}" }
        }

        override fun onSelectionChanged() {
            super.onSelectionChanged()
            val size = tracker.selection.size()
            if (size != 0) {
                if (binding.sisToolbarClear.isInvisible) binding.sisToolbarClear.isInvisible = false
                binding.sisControlContainer.isGone = false
                val set = ConstraintSet()
                set.clone(binding.sisContainer)
                set.connect(
                    binding.sisRefresh.id,
                    ConstraintSet.BOTTOM,
                    binding.sisControlContainer.id,
                    ConstraintSet.TOP
                )
                set.applyTo(binding.sisContainer)
                with(binding.sisSelectChoiceBtn) {
                    text = String.format("Выбрать %d", size)
                    onClick {
                        vm.setSelectedImages(tracker.selection.toList())
                        findNavController().navigate(_id.go_select_img_to_preview)
                    }
                }
            } else {
                if (!binding.sisToolbarClear.isInvisible) binding.sisToolbarClear.isInvisible = true
                val set = ConstraintSet()
                set.clone(binding.sisContainer)
                set.connect(
                    binding.sisRefresh.id,
                    ConstraintSet.BOTTOM,
                    binding.sisContainer.id,
                    ConstraintSet.BOTTOM
                )
                set.applyTo(binding.sisContainer)
                binding.sisControlContainer.isGone = true

            }
            debugLog { "Changed ${tracker.selection.size()}" }
        }
    }

    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            binding.sisToolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        binding.sisToolbar.hideCustomLayout()
        binding.sisToolbarBack.onClick { findNavController().navigateUp() }
        binding.sisToolbarTitle.text = "Фотопленка"


        val selectAdapter = SelectImgAdapter()

        with(binding.sisList) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = selectAdapter
        }

        keyProvider = SelectItemKeyProvider()
        tracker = SelectionTracker.Builder(
            "select_images_picker",
            binding.sisList,
            keyProvider,
            SelectItemDetailsLookup(binding.sisList),
            StorageStrategy.createLongStorage()
        ).build()

        selectAdapter.tracker = tracker
        tracker.addObserver(trackerObserverListener)

        binding.sisToolbarClear.onClick {
            tracker.clearSelection()
        }

        vm.images.observe(this, { data ->
            if (data.isNotEmpty()) {
                selectAdapter.submit(data)
                keyProvider.submit(data)
            }
        })

        vm.progressState.observe(this, { state ->
            when {
                state -> if (!binding.sisRefresh.isRefreshing) binding.sisRefresh.isRefreshing =
                    state
                else -> if (binding.sisRefresh.isRefreshing) binding.sisRefresh.isRefreshing = state
            }
        })

        binding.sisRefresh.setOnRefreshListener {
            vm.loadLastImg(true)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.sisList.addOnScrollListener(recyclerScrollListener)
        vm.loadLastImg(false)
    }

    override fun onPause() {
        binding.sisList.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

}