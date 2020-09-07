package com.isanechek.imagehandler.ui.city

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.databinding.CityParentLayoutBinding
import com.isanechek.imagehandler.delegate.viewBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.lang.IllegalArgumentException

class CityParentFragment : Fragment(_layout.city_parent_layout) {

    private val vm: SelectViewModel by sharedViewModel()
    private val binding by viewBinding(CityParentLayoutBinding::bind)

    private class SlideScreensAdapter(f: Fragment) : FragmentStateAdapter(f) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SelectCityScreen()
                1 -> SampleCityScreen()
                else -> throw IllegalArgumentException("WTF Fragment position!")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.cpPager) {
            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = SlideScreensAdapter(this@CityParentFragment)
        }
        vm.stateScreen.observe(viewLifecycleOwner, Observer { currentScreen ->
            when (currentScreen) {
                -1 -> findNavController().navigate(_id.go_select_handler)
                else -> binding.cpPager.currentItem = currentScreen
            }
        })

    }

}