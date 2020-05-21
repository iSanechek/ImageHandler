package com.isanechek.imagehandler.ui.choices.images

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.data.models.GalleryImageResult
import com.isanechek.imagehandler.ui.choices.BaseChoicesFragment

class ChoicesAllImagesFragment : BaseChoicesFragment() {

    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var keyProvider: PickerItemKeyProvider

    private val trackerObserverListener = object : SelectionTracker.SelectionObserver<Long>() {

        override fun onSelectionRestored() {
            super.onSelectionRestored()
            debugLog { "Restored ${tracker.selection.size()}" }
            setSelectedSize(tracker.selection.size())
        }

        override fun onSelectionChanged() {
            super.onSelectionChanged()
            debugLog { "Changed ${tracker.selection.size()}" }
            setSelectedSize(tracker.selection.size())
        }
    }

    private fun setSelectedSize(size: Int) {
        if (size == 0) {
            vm.setToolbarTitle("")
        } else {
            vm.setToolbarTitle(String.format("Selected %d", size))
        }
    }

    override fun bindUi(recyclerView: RecyclerView, savedInstanceState: Bundle?) {
        val imagesAdapter = ChoicesAdapter()
        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imagesAdapter
        }


        keyProvider = PickerItemKeyProvider()
        tracker = SelectionTracker.Builder(
            "all_images_picker",
            recyclerView,
            keyProvider,
            PickerItemDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).build()

        imagesAdapter.tracker = tracker
        tracker.addObserver(trackerObserverListener)
        vm.imagesData.observe(this, Observer { data ->
            when (data) {
                is GalleryImageResult.Done -> {
                    hideSRF()
                    vm.hideToolbarProgress()
                    imagesAdapter.submit(data.data)
                    keyProvider.submit(data.data)
                }
                is GalleryImageResult.Load -> {
                    vm.showToolbarProgress()
                }
                is GalleryImageResult.Error -> {
                    hideSRF()
                    vm.hideToolbarProgress()
                    imagesAdapter.submit(data.data)
                    keyProvider.submit(data.data)
                }
                is GalleryImageResult.Update -> {
                    imagesAdapter.submit(data.data)
                    keyProvider.submit(data.data)
                }
            }
        })
    }

    override fun refresh() {
        vm.loadImages(true)
    }

    override fun startLoadData() {
        vm.loadImages()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        tracker.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }


    companion object {
        fun createInstance() = ChoicesAllImagesFragment()
    }
}