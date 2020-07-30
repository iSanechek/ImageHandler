package com.isanechek.imagehandler.ui.handler.choices

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.select_img_screen_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectImgScreen : BaseFragment(_layout.select_img_screen_layout) {

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
                if (sis_toolbar_clear.isInvisible) sis_toolbar_clear.isInvisible = false
                sis_control_container.isGone = false
                val set = ConstraintSet()
                set.clone(sis_container)
                set.connect(
                    sis_refresh.id,
                    ConstraintSet.BOTTOM,
                    sis_control_container.id,
                    ConstraintSet.TOP
                )
                set.applyTo(sis_container)
                sis_select_choice_btn.text = String.format("Выбрать %d", size)
                sis_select_choice_btn.onClick {
                    vm.setSelectedImages(tracker.selection.toList())
                    findNavController().navigate(_id.go_select_img_to_preview)
                }
            } else {
                if (!sis_toolbar_clear.isInvisible) sis_toolbar_clear.isInvisible = true
                val set = ConstraintSet()
                set.clone(sis_container)
                set.connect(
                    sis_refresh.id,
                    ConstraintSet.BOTTOM,
                    sis_container.id,
                    ConstraintSet.BOTTOM
                )
                set.applyTo(sis_container)
                sis_control_container.isGone = true

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
            sis_toolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        sis_toolbar.hideCustomLayout()
        sis_toolbar_back.onClick { findNavController().navigateUp() }
        sis_toolbar_title.text = "Фотопленка"


        val selectAdapter = SelectImgAdapter()

        with(sis_list) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = selectAdapter
        }

        keyProvider = SelectItemKeyProvider()
        tracker = SelectionTracker.Builder(
            "select_images_picker",
            sis_list,
            keyProvider,
            SelectItemDetailsLookup(sis_list),
            StorageStrategy.createLongStorage()
        ).build()

        selectAdapter.tracker = tracker
        tracker.addObserver(trackerObserverListener)

        sis_toolbar_clear.onClick {
            tracker.clearSelection()
        }

        vm.images.observe(this, Observer { data ->
            if (data.isNotEmpty()) {
                selectAdapter.submit(data)
                keyProvider.submit(data)
            }
        })

        vm.progressState.observe(this, Observer { state ->
            when {
                state -> if (!sis_refresh.isRefreshing) sis_refresh.isRefreshing = state
                else -> if (sis_refresh.isRefreshing) sis_refresh.isRefreshing = state
            }
        })

        sis_refresh.setOnRefreshListener {
            vm.loadLastImg(true)
        }
    }

    override fun onStart() {
        super.onStart()
        sis_list.addOnScrollListener(recyclerScrollListener)
        vm.loadLastImg(false)
    }

    override fun onPause() {
        sis_list.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

}