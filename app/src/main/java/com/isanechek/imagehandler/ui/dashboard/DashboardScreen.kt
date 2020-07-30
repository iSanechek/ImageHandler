package com.isanechek.imagehandler.ui.dashboard

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.isanechek.imagehandler.*
import com.isanechek.imagehandler.data.models.DashboardItem
import com.isanechek.imagehandler.data.repositories.DashboardRepository
import com.isanechek.imagehandler.ui.base.BaseFragment
import com.isanechek.imagehandler.ui.base.bind
import kotlinx.android.synthetic.main.dashboard_item_layout.view.*
import kotlinx.android.synthetic.main.dashboard_screen_layout.*
import org.koin.android.ext.android.inject

class DashboardScreen : BaseFragment(_layout.dashboard_screen_layout) {

    private val repository: DashboardRepository by inject()

    private val data = listOf(
        DashboardItem(
            key = CROP_SQUARE_KEY,
            title = "обрезать\n1:1",
            description = "",
            color = _color.color_crop_1_1,
            icon = _drawable.ic_baseline_crop_square_24,
            ratioValue = ASPECT_RATIO_1_1
        ),
        DashboardItem(
            key = CROP_PORTRAIT_KEY,
            title = "обрезать\n9:16",
            description = "",
            color = _color.color_crop_9_16,
            icon = _drawable.ic_baseline_crop_portrait_24,
            ratioValue = ASPECT_RATIO_9_16
        ),
        DashboardItem(
            key = CROP_16_9_KEY,
            title = "обрезать\n16:9",
            description = "",
            color = _color.color_crop_16_9,
            icon = _drawable.ic_baseline_crop_16_9_24,
            ratioValue = ASPECT_RATIO_16_9
        ),
        DashboardItem(
            key = TAGS_KEY,
            title = "Тэги",
            description = "",
            color = _color.color_tag_s,
            icon = _drawable.ic_baseline_image_search_24,
            ratioValue = 0
        )
    )

    override fun bindUi(savedInstanceState: Bundle?) {
        dsl_list.bind(data, _layout.dashboard_item_layout) { item: DashboardItem, _: Int ->
            with(item) {
                val i = this
                dil_container.apply {
                    onClick {
                        when(item.key) {
                            CROP_SQUARE_KEY, CROP_PORTRAIT_KEY, CROP_16_9_KEY -> {
                                repository.setSelectRatio(item.ratioValue)
                                findNavController().navigate(
                                    _id.go_to_select_from_dashboard
                                )
                            }
                            else -> Unit
                        }
                    }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), i.color))
                }
                dsl_icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), this.icon))
                dsl_title.text = this.title
            }


        }.layoutManager(GridLayoutManager(requireContext(), 2))
    }

    companion object {
        const val CROP_SQUARE_KEY = "crop_square"
        const val CROP_PORTRAIT_KEY = "c_9_16"
        const val CROP_16_9_KEY = "c_16_9"
        const val TAGS_KEY = "tags_key"
        const val ARGS_KEY = "dak"
    }
}