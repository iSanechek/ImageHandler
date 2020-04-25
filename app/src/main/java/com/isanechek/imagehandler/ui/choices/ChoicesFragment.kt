package com.isanechek.imagehandler.ui.choices

import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import kotlinx.android.synthetic.main.choices_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChoicesFragment : BaseFragment(_layout.choices_fragment_layout) {

    private val toolbarTitle = arrayOf("Last images", "Folders")
    private val vm: ChoicesViewModel by sharedViewModel()
    private val viewPagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            vm.setToolbarTitle(toolbarTitle[position])
            cfl_navigation.menu.getItem(position).isChecked = true
        }
    }

    override fun activityCreated(savedInstanceState: Bundle?) {
        cfl_toolbar.hideCustomLayout()
        cfl_toolbar_close_btn.onClick { findNavController().navigateUp() }
        vm.toolbarProgressState.observe(this, Observer { isHow ->
            cfl_toolbar_progress.isInvisible = isHow
        })
        vm.toolbarTitle.observe(this, Observer { title ->
            when {
                title.isNotEmpty() -> cfl_toolbar_title.text = title
                else -> cfl_toolbar_title.text = getDefaultToolbarTitle()
            }
        })
        vm.showElevation.observe(this, Observer { show ->
            cfl_toolbar.setElevationVisibility(show)
        })

        setupViewPager()
    }

    private fun setupViewPager() {
        val fragmentsAdapter = ChoicesFragmentAdapter(this)
        with(cfl_pager) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentsAdapter
            registerOnPageChangeCallback(viewPagerListener)
        }

        cfl_navigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                _id.picker_menu_folder -> {
                    cfl_pager.currentItem = 1
                    true
                }
                _id.picker_menu_image -> {
                    cfl_pager.currentItem = 0
                    true
                }
                else -> false
            }
        }
    }

    override fun onPause() {
        cfl_pager.unregisterOnPageChangeCallback(viewPagerListener)
        super.onPause()
    }

    private fun getDefaultToolbarTitle(): String = toolbarTitle[cfl_pager.currentItem]
}