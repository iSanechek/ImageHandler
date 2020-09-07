package com.isanechek.imagehandler.ui.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
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
import com.isanechek.imagehandler.databinding.ImageHandler2FragmentLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import com.isanechek.imagehandler.ui.crop.CropScreen
import com.isanechek.imagehandler.ui.dashboard.DashboardScreen
import com.isanechek.imagehandler.ui.preview.PreviewScreen
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class ImageHandlerFragment2 : Fragment(_layout.image_handler2_fragment_layout) {

    private val binding by viewBinding(ImageHandler2FragmentLayoutBinding::bind)
    private val vm: ImageHandlerViewModel by stateViewModel()
    private lateinit var resultAdapter: ResultAdapter

    private val key: String
        get() = requireArguments().getString(DashboardScreen.ARGS_KEY, EMPTY_STRING_VALUE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        debugLog { "KEY $key" }
        when(key) {
            DashboardScreen.CROP_SQUARE_KEY -> binding.ihf2ToolbarInfo.text = "Формат 1:1"
            DashboardScreen.CROP_16_9_KEY -> binding.ihf2ToolbarInfo.text = "Формат 16:9"
            DashboardScreen.CROP_PORTRAIT_KEY -> binding.ihf2ToolbarInfo.text = "Формат 9:16"
        }

        binding.ihf2CloseBtn.onClick { findNavController().navigateUp() }

        setupBtn()
        binding.ihf2InfoText.text = "Вы можите выбрать\nодну или несколько картинок"

        binding.ihf2Container.endTransition {
            it?.progress?.let { value ->
                vm.setMotionProgressState(value)
            }
        }

        vm.progress.observe(viewLifecycleOwner, { isShow ->
            binding.ihf2ToolbarProgress.isInvisible = !isShow

        })

        vm.toast.observe(viewLifecycleOwner, { toastMsg ->
            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
        })

        vm.progressCount.observe(viewLifecycleOwner, { count ->
            binding.ihf2ToolbarCount.text = count
        })

        resultAdapter = ResultAdapter()
        with(binding.ihf2ResultList) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = resultAdapter

        }

        resultAdapter.setOnClickListener(object : ResultAdapter.OnClickListener {
            override fun itemClick(item: ImageItem) {
                findNavController().navigate(
                    _id.go_preview_from_handler,
                    bundleOf(
                        PreviewScreen.ITEM_ID_KEY to item.id,
                        PreviewScreen.PATH_PREVIEW_KEY to item.originalPath
                    )
                )
            }

            override fun itemLongClick(item: ImageItem) {
                MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    lifecycleOwner(this@ImageHandlerFragment2)
                    listItems(items = listOf("редактировать", "удалить")) { _, index, _ ->
                        when (index) {
                            0 -> findNavController().navigate(
                                _id.go_crop_from_handler,
                                bundleOf(
                                    CropScreen.LAUNCH_TYPE_KEY to CropScreen.EDIT_TYPE_VALUE,
                                    CropScreen.CROP_ID_KEY to item.id,
                                    CropScreen.CROP_PATH_KEY to item.originalPath
                                )
                            )
                            1 -> {
                                MaterialDialog(requireContext()).show {
                                    lifecycleOwner(this@ImageHandlerFragment2)
                                    title(text = "Внимание")
                                    message(text = "Удалить ${item.name}?")
                                    positiveButton(text = "удалить") { dialog ->
                                        dialog.dismiss()
                                        vm.removeItem(item)
                                    }
                                    negativeButton(text = "отменить")
                                }
                            }
                        }
                    }
                }
            }
        })

        vm.data.observe(viewLifecycleOwner, { data ->
            resultAdapter.submit(data)
            binding.ihf2ResultList.smoothScrollToPosition(0)
        })

        lifecycleScope.launchWhenResumed {

            vm.result.collect { data ->
                debugLog { "RESULT DATA ${data.size}" }
                if (data.isNotEmpty()) {
                    resultAdapter.submit(data)
                }
            }
        }

        vm.getMotionProgress.observe(viewLifecycleOwner, { progress ->
            debugLog { "PROGRESS $progress" }
            binding.ihf2Container.progress = progress
        })

        vm.saveProgressState.observe(viewLifecycleOwner, { state ->
            binding.ihf2SaveBtn.apply {
                setStateProgress(state)
                setEnableState(state)
            }
            binding.ihf2ClearBtn.setEnableState(!state)
            binding.ihf2OverlayBtn.apply {
                setEnableState(!state)
                setEnableState(false)
            }
        })

        vm.workState.observe(
            viewLifecycleOwner,
            { state ->
                binding.ihf2OverlayBtn.apply {
                    setStateProgress(state)
                    setEnableState(state)
                }
                binding.ihf2ClearBtn.setEnableState(!state)
                binding.ihf2SaveBtn.setEnableState(!state)
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
                    message(
                        text = "На данный момент приложение поддерживает соотношение сторон ТОЛЬКО 1:1 - квадрат(лента инстаграма)." +
                                "\nХоть приложение и может определять не подходящие соотношение сторон и даже имеет инструмент для обрезки изображения, " +
                                "рекомендую пока предварительно обрезать изображение через фоторедактор Snapseed. " +
                                "Так как данные функции не всегда работают корректно." +
                                "\nСо стабильностью тоже все плохо. ((" +
                                "\n\nСпасибо за понимание.)"
                    )
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
                    binding.ihf2Container.transitionToEnd()
                }
                else -> {
                    val uri = data?.data
                    if (uri != null) {
                        val realPath = FileUtils.getPath(requireContext(), uri)
                        debugLog { "IMAGE PATH $realPath" }
                        vm.setData(listOf(realPath))
                        binding.ihf2Container.transitionToEnd()
                    } else showErrorMessage("URI PATH IS NULL")
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupBtn() {
        // settings
        binding.ihf2ToolbarSetting.onClick { settingDialog() }

        // choice images
        binding.ihf2ChoiceBtn.onClick {
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
        binding.ihf2ClearBtn.apply {
            setTitle("очистка")
            setIcon(_drawable.ic_baseline_clear_24)
            onClick {
                vm.clearData()
                resultAdapter.clear()
                binding.ihf2Container.transitionToStart()
                delay(300) {
                    binding.ihf2SaveBtn.setEnableState(true)
                    binding.ihf2OverlayBtn.setEnableState(true)
                }
            }
        }

        // overlay button
        binding.ihf2OverlayBtn.apply {
            setTitle("нанести")
            setIcon(_drawable.ic_baseline_compare_24)
            onClick {
                vm.startWork()
            }
        }

        // save btn
        binding.ihf2SaveBtn.apply {
            setTitle("сохранить")
            setIcon(_drawable.ic_baseline_save_24)
            setEnableState(false)
            onClick {
                askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                    if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                        binding.ihf2OverlayBtn.isInvisible = true
                        binding.ihf2ClearBtn.isInvisible = true
                        vm.saveToSystem()
                    }
                }
            }
        }
    }

    private fun settingDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Меню")
            val data = listOf("изменить город")
            lifecycleOwner(this@ImageHandlerFragment2)
            listItems(items = data, waitForPositiveButton = false) { _, index, _ ->
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