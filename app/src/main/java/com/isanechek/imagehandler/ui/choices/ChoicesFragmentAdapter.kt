package com.isanechek.imagehandler.ui.choices

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isanechek.imagehandler.ui.choices.folders.ChoicesFoldersFragment
import com.isanechek.imagehandler.ui.choices.images.ChoicesAllImagesFragment

class ChoicesFragmentAdapter(fm: Fragment) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ChoicesAllImagesFragment.createInstance()
            1 -> ChoicesFoldersFragment.createInstance()
            else -> ChoicesAllImagesFragment.createInstance()
        }
    }

}