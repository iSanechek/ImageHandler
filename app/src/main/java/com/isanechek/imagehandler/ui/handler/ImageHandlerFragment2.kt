package com.isanechek.imagehandler.ui.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.utils.FileUtils
import glimpse.coil.GlimpseTransformation
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.image_choice_item_layout.*
import kotlinx.android.synthetic.main.image_handler2_fragment_layout.*
import kotlinx.android.synthetic.main.image_result_item_layout.*
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

data class ImageItem(
    val id: String,
    val name: String,
    val originalPath: String,
    val resultPath: String,
    val publicPath: String,
    val overlayStatus: String
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
                iri_cover.load(File(item.resultPath))
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

    // Choice
    inner class ChoiceAdapter : RecyclerView.Adapter<ChoiceAdapter.ChoiceHolder>() {

        inner class ChoiceHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView),
            LayoutContainer {

            fun bind(item: ImageItem) {
                ici_cover.load(File(item.originalPath)) {
                    crossfade(true)
                    transformations(GlimpseTransformation())
                }
            }
        }

        private val items = mutableListOf<ImageItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder =
            ChoiceHolder(parent.inflate(_layout.image_choice_item_layout))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
            holder.bind(items[position])
        }

        fun submit(data: List<ImageItem>) {
            if (items.isNotEmpty()) items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }
    }

    // Fragment
    private val vm: ImageHandlerViewModel by viewModel()
    private lateinit var resultAdapter: ResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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


        ihf2_settings_btn.onClick { findNavController().navigate(_id.handler_go_to_watermark) }
        ihf2_clear_btn.onClick { vm.clearData() }
        ihf2_overlay_btn.onClick {
            vm.startWork()
            resultAdapter.clear()
        }
        ihf2_save_btn.onClick {
            askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    vm.saveToSystem()
                }
            }
        }
        bindChoiceList()
        bindResultList()

        lifecycleScope.launchWhenResumed {
            vm.error.collect { msg -> debugLog { "MSG $msg" } }
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

    private fun bindResultList() {
        resultAdapter = ResultAdapter()
        with(ihf2_result_list) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = resultAdapter
        }

        lifecycleScope.launchWhenResumed {


            vm.result.collect { data ->
                debugLog { "RESULT DATA ${data.size}" }
                if (data.isNotEmpty()) {
                    resultAdapter.submit(data)
                }
            }
        }
    }

    private fun bindChoiceList() {
        val choiceAdapter = ChoiceAdapter()

        with(ihf2_choice_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = choiceAdapter
        }

        lifecycleScope.launchWhenResumed {
            vm.data.collect { data ->
                debugLog { "Choice size ${data.size}" }
                if (data.isNotEmpty()) {
                    ihf2_container.transitionToEnd()
                } else ihf2_container.transitionToStart()

                choiceAdapter.submit(data)
            }
        }
    }

    private fun showErrorMessage(message: String) {
        debugLog {
            "ERROR $message"
        }
    }

}