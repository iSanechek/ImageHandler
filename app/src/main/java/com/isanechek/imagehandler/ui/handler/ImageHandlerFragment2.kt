package com.isanechek.imagehandler.ui.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
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
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.local.database.entity.ImageItem
import com.isanechek.imagehandler.ui.widgets.MultiStateButton
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.android.synthetic.main.image_handler2_fragment_layout.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImageHandlerFragment2 : Fragment(_layout.image_handler2_fragment_layout) {

    private val vm: ImageHandlerViewModel by stateViewModel()
    private lateinit var resultAdapter: ResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBtn()
        ihf2_info_text.text = "Вы можите выбрать\nодну или несколько картинок"

        ihf2_container.endTransition {
            it?.progress?.let { value ->
                vm.setMotionProgressState(value)
            }
        }

        vm.progress.observe(viewLifecycleOwner, Observer { isShow ->
            ihf2_toolbar_progress.isInvisible = !isShow

        })

        vm.toast.observe(viewLifecycleOwner, Observer { toastMsg ->
            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
        })

        vm.progressCount.observe(viewLifecycleOwner, Observer { count ->
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
            vm.data.collect { data ->
                resultAdapter.submit(data)
            }

            vm.result.collect { data ->
                debugLog { "RESULT DATA ${data.size}" }
                if (data.isNotEmpty()) {
                    resultAdapter.submit(data)

                }
            }
        }

        vm.getMotionProgress.observe(viewLifecycleOwner, Observer { progress ->
            debugLog { "PROGRESS $progress" }
            ihf2_container.progress = progress
        })

        vm.saveProgressState.observe(viewLifecycleOwner, Observer { state ->
            ihf2_save_btn.setStateProgress(state)
            ihf2_clear_btn.setEnableState(!state)
            ihf2_overlay_btn.setEnableState(!state)
        })

        vm.workState.observe(
            viewLifecycleOwner,
            Observer { state ->
                ihf2_overlay_btn.setStateProgress(state)
                ihf2_clear_btn.setEnableState(!state)
                ihf2_save_btn.setEnableState(!state)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (vm.isShowWarningDialog(IMAGE_RATIO_WARNING_KEY)) {
            delay(500) {
                MaterialDialog(requireContext()).show {
                    title(text = "Предупреждение")
                    cancelOnTouchOutside(false)
                    lifecycleOwner(this@ImageHandlerFragment2)
                    message(text = "На данный момент приложение поддерживает соотношение сторон ТОЛЬКО 1:1 - квадрат(лента инстаграма)." +
                            "\nХоть приложение и может определять не подходящие соотношение сторон и даже имеет инструмент для обрезки изображения, " +
                            "рекомендую пока предварительно обрезать изображение через фоторедактор Snapseed. " +
                            "Так как данные функции не всегда работают корректно." +
                            "\nСо стабильностью тоже все плохо. ((" +
                            "\n\nСпасибо за понимание.)")
                    positiveButton(text = "понятно") {
                        vm.markDoneDialog(IMAGE_RATIO_WARNING_KEY)
                    }
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
                    ihf2_container.transitionToEnd()
                }
                else -> {
                    val uri = data?.data
                    if (uri != null) {
                        val realPath = FileUtils.getPath(requireContext(), uri)
                        debugLog { "IMAGE PATH $realPath" }
                        vm.setData(listOf(realPath))
                        ihf2_container.transitionToEnd()
                    } else showErrorMessage("URI PATH IS NULL")
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupBtn() {
        // settings
        ihf2_toolbar_setting.onClick { settingDialog() }

        // choice images
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

        // clear content
        ihf2_clear_btn.apply {
            setTitle("очистка")
            setIcon(_drawable.ic_baseline_clear_24)
            onClick {
                vm.clearData()
                resultAdapter.clear()
                ihf2_container.transitionToStart()
            }
        }

        // overlay button
        ihf2_overlay_btn.apply {
            setTitle("нанести")
            setIcon(_drawable.ic_baseline_compare_24)
            onClick {
                vm.startWork()
            }
        }

        // save btn
        ihf2_save_btn.apply {
            setTitle("сохранить")
            setIcon(_drawable.ic_baseline_save_24)
            setEnableState(false)
            onClick {
                askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                    if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                        ihf2_overlay_btn.isInvisible = true
                        ihf2_clear_btn.isInvisible = true
                        vm.saveToSystem()
                    }
                }
            }
        }
    }

    private fun settingDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Menu")
            val data = listOf("overlay")
            lifecycleOwner(this@ImageHandlerFragment2)
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

    companion object {
        private const val IMAGE_RATIO_WARNING_KEY = "irwk"
    }
}