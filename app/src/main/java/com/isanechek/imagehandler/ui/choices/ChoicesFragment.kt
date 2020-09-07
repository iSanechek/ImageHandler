package com.isanechek.imagehandler.ui.choices

import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.ChoicesFragmentLayoutBinding
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChoicesFragment : BaseFragment(_layout.choices_fragment_layout) {

    private lateinit var containerView: ChoicesFragmentLayoutBinding
    private val toolbarTitle = arrayOf("Last images", "Folders")
    private val vm: ChoicesViewModel by sharedViewModel()
    private val viewPagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            vm.setToolbarTitle(toolbarTitle[position])
            containerView.cflNavigation.menu.getItem(position).isChecked = true
        }
    }

    override fun bindUi(savedInstanceState: Bundle?) {
        containerView = ChoicesFragmentLayoutBinding.inflate(layoutInflater)
        containerView.cflToolbar.hideCustomLayout()
        containerView.cflToolbarCloseBtn.onClick { findNavController().navigateUp() }
        vm.toolbarProgressState.observe(this, Observer { isHow ->
            containerView.cflToolbarProgress.isInvisible = isHow
        })
        vm.toolbarTitle.observe(this, Observer { title ->
            when {
                title.isNotEmpty() -> containerView.cflToolbarTitle.text = title
                else -> containerView.cflToolbarTitle.text = getDefaultToolbarTitle()
            }
        })
        vm.showElevation.observe(this, Observer { show ->
            containerView.cflToolbar.setElevationVisibility(show)
        })

        setupViewPager()
    }

    private fun setupViewPager() {
        val fragmentsAdapter = ChoicesFragmentAdapter(this)
        with(containerView.cflPager) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = fragmentsAdapter
            registerOnPageChangeCallback(viewPagerListener)
        }

        containerView.cflNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                _id.picker_menu_folder -> {
                    containerView.cflPager.currentItem = 1
                    true
                }
                _id.picker_menu_image -> {
                    containerView.cflPager.currentItem = 0
                    true
                }
                else -> false
            }
        }
    }

    override fun onPause() {
        containerView.cflPager.unregisterOnPageChangeCallback(viewPagerListener)
        super.onPause()
    }

    private fun getDefaultToolbarTitle(): String = toolbarTitle[containerView.cflPager.currentItem]
}