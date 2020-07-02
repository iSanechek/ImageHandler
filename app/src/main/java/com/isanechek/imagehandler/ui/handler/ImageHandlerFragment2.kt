package com.isanechek.imagehandler.ui.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import androidx.room.Entity
import coil.api.load
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.utils.FileUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.image_handler2_fragment_layout.*
import kotlinx.android.synthetic.main.image_result_item_layout.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

@Entity(tableName = "image_table", primaryKeys = ["id"])
data class ImageItem(
    val id: String,
    val name: String,
    @ColumnInfo(name = "original-path") val originalPath: String,
    @ColumnInfo(name = "result_path") val resultPath: String,
    @ColumnInfo(name = "public_path") val publicPath: String,
    @ColumnInfo(name = "overlay_status") val overlayStatus: String
) {

    companion object {
        const val OVERLAY_FAIL = "fail"
        const val OVERLAY_DONE = "done"
        const val OVERLAY_NONE = "none"
    }
}

class ImageHandlerFragment2 : Fragment(_layout.image_handler2_fragment_layout) {

    // Result
    inner class ResultAdapter : RecyclerView.Adapter<ResultAdapter.ResultHolder>() {

        inner class ResultHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView),
            LayoutContainer {

            fun bind(item: ImageItem) {
                debugLog { item.toString() }
                val path = if (item.overlayStatus == ImageItem.OVERLAY_DONE) item.resultPath else item.originalPath
                debugLog { "PATH $path" }
                iri_cover.load(File(path))
            }
        }

        private val items = mutableListOf<ImageItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder =
            ResultHolder(parent.inflate(_layout.image_result_item_layout))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.bind(items[position])
        }

        fun submit(data: List<ImageItem>) {
            if (items.isNotEmpty()) items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }

        fun clear() {
            if (items.isNotEmpty()) items.clear()
        }
    }

    // Fragment
    private val vm: ImageHandlerViewModel by viewModel()
    private lateinit var resultAdapter: ResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ihf2_toolbar_title.text = "Вова"
        ihf2_toolbar_setting.onClick { settingDialog() }

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
                } else showErrorMessage("PERMISSION NOT GRANTED")
            }
        }

        ihf2_clear_btn.onClick {
            vm.clearData()
            resultAdapter.clear()
        }
        ihf2_overlay_btn.onClick {
            vm.startWork()
        }
        ihf2_save_btn.onClick {
            askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    vm.saveToSystem()
                }
            }
        }
        bindResultList()

        vm.progress.observe(viewLifecycleOwner, Observer { isShow ->
            ihf2_toolbar_progress.isInvisible = !isShow
        })

        vm.toast.observe(viewLifecycleOwner, Observer { toastMsg ->
            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
        })

        vm.progressCount.observe(viewLifecycleOwner, Observer { count ->
            debugLog { count }
            ihf2_toolbar_count.text = count
        })
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

    private fun bindResultList() {
        resultAdapter = ResultAdapter()
        with(ihf2_result_list) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = resultAdapter
        }

        lifecycleScope.launchWhenResumed {

            vm.data.collect { data ->
                debugLog { "Choice size ${data.size}" }
                if (data.isNotEmpty()) {
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

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        debugLog {
            "ERROR $message"
        }
    }

}