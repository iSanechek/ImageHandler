package com.isanechek.imagehandler.ui.choices.folders

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.imagehandler.data.models.ChoicesResult
import com.isanechek.imagehandler.ui.choices.BaseChoicesFragment

class ChoicesFoldersFragment : BaseChoicesFragment() {

    override fun bindUi(recyclerView: RecyclerView, savedInstanceState: Bundle?) {
        vm.loadFolders(false)
        val foldersAdapter = ChoicesFoldersAdapter { item -> }
        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = foldersAdapter
        }
//        vm.folders.observe(this, Observer { result ->
//            when(result) {
//                is ChoicesResult.Error -> {
//                    vm.hideToolbarProgress()
//                    foldersAdapter.submit(result.data)
//                }
//                is ChoicesResult.Load -> {
//                    vm.showToolbarProgress()
//                }
//                is ChoicesResult.Done -> {
//                    vm.hideToolbarProgress()
//                    foldersAdapter.submit(result.data)
//                }
//                is ChoicesResult.Update -> {
//                    vm.showToolbarProgress()
//                    foldersAdapter.submit(result.data)
//                }
//            }
//        })

        vm.choice.observe(this, Observer { data ->
            when(data) {
                is ChoicesResult.Error -> {}
                is ChoicesResult.Done -> {
                    foldersAdapter.submit(data.data)
                }
                is ChoicesResult.Load -> {}
                is ChoicesResult.Update -> {}
            }
        })
    }

    override fun refresh() {

    }

    override fun startLoadData() {
        vm.loadFolders()
    }

    companion object {
        fun createInstance() = ChoicesFoldersFragment()
    }

}