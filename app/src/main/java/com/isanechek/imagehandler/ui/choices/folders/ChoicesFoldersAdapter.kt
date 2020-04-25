package com.isanechek.imagehandler.ui.choices.folders

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.data.models.Folder
import com.isanechek.imagehandler.inflate
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.ui.choices.childs.BaseViewHolder
import kotlinx.android.synthetic.main.choices_folder_item_layout.*
import java.io.File

class ChoicesFoldersAdapter(private val callback: (Folder) -> Unit) :
    RecyclerView.Adapter<ChoicesFoldersAdapter.ChoicesFolderHolder>() {

    inner class ChoicesFolderHolder(override val containerView: View) :
        BaseViewHolder<Folder>(containerView) {

        override fun bindItem(item: Folder, callback: (Folder) -> Unit) {
            cfi_container.onClick { callback.invoke(item) }
            cfi_cover.load(File(item.caverPaths.first()))
            cfi_name.text = item.name
        }
    }

    private val folders = mutableListOf<Folder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoicesFolderHolder =
        ChoicesFolderHolder(parent.inflate(_layout.choices_folder_item_layout))

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(holder: ChoicesFolderHolder, position: Int) {
        holder.bindItem(folders[position], callback)
    }

    override fun onViewAttachedToWindow(holder: ChoicesFolderHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAppear()
    }

    override fun onViewDetachedFromWindow(holder: ChoicesFolderHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDisappear()
    }

    fun submit(data: List<Folder>) {
        if (folders.isNotEmpty()) folders.clear()
        folders.addAll(data)
        notifyDataSetChanged()
    }
}