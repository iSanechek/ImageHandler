package com.isanechek.imagehandler.ui2.images

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.UiState
import com.isanechek.imagehandler.databinding.ImagesScreenLayoutBinding
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.utils.isRequestPermission
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImagesScreen : BaseFragment(_layout.images_screen_layout) {

    private val binding by viewBinding(ImagesScreenLayoutBinding::bind)
    private val vm: ImagesViewModel by viewModel()

    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                vm.loadLastImages()
            } else {
                debugLog { "Permission fuck" }
            }
        }

    override fun bindUi(savedInstanceState: Bundle?) {

        isRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) { result ->
            when (result) {
                "request" -> vm.setHomeState(UiState.Permission)
                "load" -> vm.loadLastImages()
            }
        }

        val imagesAdapter = ImagesAdapter()

        with(binding.isList) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imagesAdapter
        }

        vm.uiState.observe(this, { state ->

            debugLog { "State $state" }
            when (state) {
                is UiState.Progress -> {
                    binding.isContainer.apply {
                        setTransition(_id.images_default_to_progress_state)
                        transitionToEnd()
                    }
                }
                is UiState.Done -> {
                    binding.isRefresh.apply {
                        if (isRefreshing) isRefreshing = false
                    }
                    binding.isContainer.apply {
                        setTransition(_id.images_default_to_data_state)
                        transitionToEnd()
                    }
                    imagesAdapter.submit(state.data)
                }
                is UiState.Error -> {
                    binding.isContainer.apply {
                        setTransition(_id.images_permission_to_error_state)
                        transitionToEnd()
                    }
                }
                is UiState.Permission -> {
                    binding.isContainer.apply {
                        setTransition(_id.images_default_to_permission_state)
                        transitionToEnd()
                    }
                }
            }
        })

        binding.isRefresh.setOnRefreshListener {
            isRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) { result ->
                when (result) {
                    "request" -> vm.setHomeState(UiState.Permission)
                    "load" -> vm.refreshLastImages()
                }
            }
        }

//        binding.card_permission_btn.onClick {
//            storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
    }

    private fun showInfoRequestPermission() {

    }
}