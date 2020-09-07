package com.isanechek.imagehandler.ui.choices

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.isanechek.imagehandler._id
import com.isanechek.imagehandler._layout
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseChoicesFragment : Fragment(_layout.base_choices_fragment_layout) {

    val vm: ChoicesViewModel by sharedViewModel()

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
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
        refreshLayout = view.findViewById(_id.bcf_refresh)
        refreshLayout.setOnRefreshListener { refresh() }

        recyclerView = view.findViewById(_id.bcf_list)
        bindUi(recyclerView, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        recyclerView.addOnScrollListener(recyclerScrollListener)
        startLoadData()
    }

    override fun onPause() {
        recyclerView.removeOnScrollListener(recyclerScrollListener)
        super.onPause()
    }

    fun showSRF() {
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
    }

    fun hideSRF() {
        if (refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = false
        }
    }
}