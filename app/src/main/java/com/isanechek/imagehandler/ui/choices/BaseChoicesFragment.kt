package com.isanechek.imagehandler.ui.choices

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler._layout
import kotlinx.android.synthetic.main.base_choices_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseChoicesFragment : Fragment(_layout.base_choices_fragment_layout) {

    val vm: ChoicesViewModel by sharedViewModel()
    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            vm.showToolbarElevation(recyclerView.canScrollVertically(-1))
        }
    }

    abstract fun bindUi(recyclerView: RecyclerView, savedInstanceState: Bundle?)
    abstract fun refresh()
    abstract fun startLoadData()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bcf_refresh.setOnRefreshListener { refresh() }
        bindUi(bcf_list, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        bcf_list.addOnScrollListener(recyclerScrollListener)
        startLoadData()
    }

    override fun onPause() {
        bcf_list.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

    fun showSRF() {
        if (!bcf_refresh.isRefreshing) {
            bcf_refresh.isRefreshing = true
        }
    }

    fun hideSRF() {
        if (bcf_refresh.isRefreshing) {
            bcf_refresh.isRefreshing = false
        }
    }
}