package com.isanechek.imagehandler.ui.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.android.synthetic.main.image_handler2_fragment_layout.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImageHandlerFragment2 : Fragment(_layout.image_handler2_fragment_layout) {

    private val vm: ImageHandlerViewModel by viewModel()
    private lateinit var resultAdapter: ResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ihf2_toolbar_title.text = "Вова"
        ihf2_toolbar_setting.onClick { settingDialog() }

        ihf2_info_text.text = "Вы можите выбрать\nодну или несколько картинок"

        ihf2_choice_btn.onClick {
            askForPermissions(Permission.READ_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.READ_EXTERNAL_STORAGE)) {
                    Intent().run {
                        type = "image/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(this, "Select picture"),
                            404
                        )
                    }
//                    findNavController().navigate(_id.go_choice_from_handler)
                } else showErrorMessage("PERMISSION NOT GRANTED")
            }
        }

        ihf2_clear_btn.onClick {
            vm.clearData()
            resultAdapter.clear()
        }
        ihf2_overlay_btn.onClick {
            ihf2_clear_btn.isInvisible = true
            ihf2_save_btn.isInvisible = true
            vm.startWork()
        }
        ihf2_save_btn.onClick {
            askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    ihf2_overlay_btn.isInvisible = true
                    ihf2_clear_btn.isInvisible = true
                    vm.saveToSystem()
                }
            }
        }

        vm.progress.observe(viewLifecycleOwner, Observer { isShow ->
            ihf2_toolbar_progress.isInvisible = !isShow
            if (!isShow) {
                if (ihf2_save_btn.isInvisible) ihf2_save_btn.isInvisible = false
                if (ihf2_clear_btn.isInvisible) ihf2_clear_btn.isInvisible = false
                if (ihf2_overlay_btn.isInvisible) ihf2_overlay_btn.isInvisible = false
            }
        })

        vm.toast.observe(viewLifecycleOwner, Observer { toastMsg ->
            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
        })

        vm.progressCount.observe(viewLifecycleOwner, Observer { count ->
            debugLog { count }
            ihf2_toolbar_count.text = count
        })

        resultAdapter = ResultAdapter()
        with(ihf2_result_list) {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = resultAdapter
        }

        resultAdapter.setOnClickListener(object : ResultAdapter.OnClickListener {
            override fun itemClick(item: ImageItem) {
                findNavController().navigate(
                    _id.go_crop_from_handler,
                    bundleOf("crop_id" to item.id, "crop_origin_path" to item.originalPath)
                )
            }
        })

        lifecycleScope.launchWhenResumed {

            debugLog {
                "W ${ihf2_content_container.width} & H ${ihf2_content_container.height}"
            }

            vm.data.collect { data ->
                debugLog { "Choice size ${data.size}" }
                if (data.find { it.overlayStatus == ImageItem.OVERLAY_NONE } != null) {
                    ihf2_container.transitionToEnd()
                } else ihf2_container.transitionToStart()
                resultAdapter.submit(data)
            }

            vm.result.collect { data ->
                debugLog { "RESULT DATA ${data.size}" }
                if (data.isNotEmpty()) {
                    resultAdapter.submit(data)

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 404 && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            when {
                clipData != null -> {
                    val temp = mutableListOf<String>()
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i)
                        if (uri != null) {
                            temp.add(FileUtils.getPath(requireContext(), uri.uri))
                        }
                    }

                    debugLog { "LIST PATH SIZE ${temp.size}" }
                    vm.setData(temp)
                }
                else -> {
                    val uri = data?.data
                    if (uri != null) {
                        val realPath = FileUtils.getPath(requireContext(), uri)
                        debugLog { "IMAGE PATH $realPath" }
                        vm.setData(listOf(realPath))
                    } else showErrorMessage("URI PATH IS NULL")
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun settingDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Menu")
            val data = listOf("overlay")
            listItems(items = data, waitForPositiveButton = false) { dialog, index, _ ->
                when (index) {
                    0 -> findNavController().navigate(
                        _id.go_handler_to_select,
                        bundleOf("sample_args" to "handler")
                    )
                    else -> Unit
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        debugLog {
            "ERROR $message"
        }
    }

}