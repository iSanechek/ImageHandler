package com.isanechek.imagehandler.ui.imagehandler

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.android.synthetic.main.image_handler_fragment_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ImageHandlerFragment : BaseFragment(_layout.image_handler_fragment_layout) {

    private val vm: ImageHandlerViewModel by viewModel()

    private val listScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            when {
                dy > 0 && ih_fab.isVisible -> ih_fab.hide()
                dy < 0 && !ih_fab.isVisible -> ih_fab.show()
            }
            ih_toolbar.setElevationVisibility(recyclerView.canScrollVertically(-1))
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("SetTextI18n")
    override fun activityCreated(savedInstanceState: Bundle?) {

        // toolbar
        ih_toolbar_title.text = "Watermark"
        ih_app_bar.outlineProvider = null

        ih_delete_all_btn.onClick {
            showWarningDialog("Предупреждение", "Удалить все?") { isOk ->
                if (isOk) {
                    vm.clearAll()
                } else {
                    showToast(requireActivity(), "Удаление отменино")
                }
            }
        }
        ih_marker_btn.onClick { vm.startWork() }
        ih_settings_btn.onClick { findNavController().navigate(_id.go_handler_to_select_watermark) }
        ih_save_all_btn.onClick { vm.saveAll() }

        setupFab()
        setupList()

        vm.showToast.observe(this, Observer { msg ->
            showToast(requireActivity(), msg)
        })

        vm.progressMessage.observe(this, Observer { msg->
            d { "Progress $msg" }
        })

        vm.progressState.observe(this, Observer { show ->
            ihf_toolbar_progress.isInvisible = !show
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                val temp = mutableListOf<String>()
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i)
                    if (uri != null) {
                        temp.add(FileUtils.getPath(requireContext(), uri.uri) ?: "")
                    }
                }

                vm.startAction(temp)

            } else {
                data?.data?.let {
                    vm.startAction(FileUtils.getPath(requireContext(), it))
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        ih_list.addOnScrollListener(listScrollListener)
    }

    override fun onPause() {
        ih_list.removeOnScrollListener(listScrollListener)
        super.onPause()
    }

    private fun setupFab() {
        ih_fab.onClick {
            askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    d { "AllGranted" }
                    Intent().run {
                        type = "image/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(this, "Select picture"),
                            REQUEST_CODE
                        )
                    }

                } else if (result.isAllDenied(Permission.WRITE_EXTERNAL_STORAGE)) {
                    d { "AllDenied" }
                }
            }
        }
    }

    private fun setupList() {
        val imageHandlerAdapter = ImageHandlerAdapter { callback -> }
        val glm = GridLayoutManager(requireContext(), 2)
        with(ih_list) {
            setHasFixedSize(true)
            layoutManager = glm
            adapter = imageHandlerAdapter
        }

        vm.scrollRecycler.observe(this, Observer { position ->
            val currentVisibleLastPosition = glm.findLastVisibleItemPosition()
            if (position > currentVisibleLastPosition) {
                ih_list.smoothScrollToPosition(position)
            }
        })

        vm.data.observe(this, Observer { data ->
            imageHandlerAdapter.submitList(data)
            setupDeleteAllBtn(data.isEmpty())
        })
    }

    private fun setupDeleteAllBtn(isEmpty: Boolean) {
        if (isEmpty) {
            if (!ih_delete_all_btn.isInvisible) ih_delete_all_btn.isInvisible = true
            if (!ih_marker_btn.isInvisible) ih_marker_btn.isInvisible = true
            if (!ih_save_all_btn.isInvisible) ih_save_all_btn.isInvisible = true
        } else {
            if (ih_delete_all_btn.isInvisible) ih_delete_all_btn.isInvisible = false
            if (ih_marker_btn.isInvisible) ih_marker_btn.isInvisible = false
            if (ih_save_all_btn.isInvisible) ih_save_all_btn.isInvisible = false
        }
    }

    private fun fixPath(path: String): Uri = FileProvider.getUriForFile(
        requireActivity(),
        BuildConfig.APPLICATION_ID + ".provider",
        File(path)
    )

    companion object {
        private const val REQUEST_CODE = 606
    }

}