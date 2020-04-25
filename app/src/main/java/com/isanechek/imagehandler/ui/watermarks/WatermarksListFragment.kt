package com.isanechek.imagehandler.ui.watermarks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.isanechek.imagehandler._drawable
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.watermark_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class WatermarksListFragment : BaseFragment(_layout.watermark_fragment_layout) {

    private val vm: WatermarksListViewModel by viewModel()

    private val listScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) { watermark_toolbar.setElevationVisibility(recyclerView.canScrollVertically(-1)) }
    }

    override fun activityCreated(savedInstanceState: Bundle?) {

        with(watermark_toolbar) {
            setBackOrCloseButton {
                findNavController().navigateUp()
            }
            setSettingButton(isSettings = false, iconId = _drawable.ic_baseline_add_24) {
                askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { result ->
                    if (result.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                        d { "AllGranted" }
                        Intent().run {
                            type = "image/*"
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

        val watermarkAdapter = WatermarksListAdapter { item ->
            vm.selectWatermark(item.id)
        }

        with(watermark_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = watermarkAdapter
        }

        vm.data.observe(this, Observer { data ->
            watermarkAdapter.submit(data)
        })
    }

    override fun onResume() {
        super.onResume()
        watermark_list.addOnScrollListener(listScrollListener)
    }

    override fun onPause() {
        watermark_list.removeOnScrollListener(listScrollListener)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { vm.addWatermark(it) }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val REQUEST_CODE = 101
    }

}