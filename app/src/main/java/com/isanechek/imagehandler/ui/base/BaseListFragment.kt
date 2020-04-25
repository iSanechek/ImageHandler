package com.isanechek.imagehandler.ui.base

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._layout
import kotlinx.android.synthetic.main.base_list_fragment_layout.*

abstract class BaseListFragment : BaseFragment(_layout.base_list_fragment_layout) {

    abstract fun bindUi(savedInstanceState: Bundle?)

    override fun activityCreated(savedInstanceState: Bundle?) {

        bindUi(savedInstanceState)
    }


    fun list(): RecyclerView = blf_list
}