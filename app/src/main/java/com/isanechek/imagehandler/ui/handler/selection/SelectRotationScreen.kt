package com.isanechek.imagehandler.ui.handler.selection

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanechek.imagehandler.ASPECT_RATIO_16_9
import com.isanechek.imagehandler.ASPECT_RATIO_1_1
import com.isanechek.imagehandler.ASPECT_RATIO_9_16
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.data.models.SelectionRationItem
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.select_rotation_screen_layout.*
import org.koin.android.ext.android.inject

class SelectRotationScreen : BaseFragment(_layout.select_rotation_screen_layout) {

    private val prefManager: PrefManager by inject()

    private val rationList = listOf(
        SelectionRationItem("i_timeline", ASPECT_RATIO_1_1, prefManager.sampleImagePath, "1:1", "Этот формат подходит для ленты Instagram"),
        SelectionRationItem("i_stories", ASPECT_RATIO_9_16, prefManager.sampleImagePath, "9:16", "Этот формат подходит для историй Instagram"),
        SelectionRationItem("other_social", ASPECT_RATIO_16_9, prefManager.sampleImagePath, "16:9", "Это формат подходит для Вконтакте, Одноклассников итд")
    )

    override fun bindUi(savedInstanceState: Bundle?) {

        val rationAdapter = RationAdapter(rationList)

        with(srs_list) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rationAdapter
        }
    }
}