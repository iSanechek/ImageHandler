package com.isanechek.imagehandler.ui.choices.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.imagehandler.data.models.Folder
import com.isanechek.imagehandler.databinding.ChoicesFolderItemLayoutBinding
import com.isanechek.imagehandler.onClick
import java.io.File

class ChoicesFoldersAdapter(private val callback: (Folder) -> Unit) :
    RecyclerView.Adapter<ChoicesFoldersAdapter.ChoicesFolderHolder>() {

    class ChoicesFolderHolder(val containerView: ChoicesFolderItemLayoutBinding) :
        RecyclerView.ViewHolder(containerView.root) {

        fun bindItem(item: Folder, callback: (Folder) -> Unit) {
            with(containerView) {
                cfiContainer.onClick { callback.invoke(item) }
                cfiCover.load(File(item.caverPaths.first()))
                cfiName.text = item.name
            }
        }
    }

    private val folders = mutableListOf<Folder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoicesFolderHolder =
        ChoicesFolderHolder(ChoicesFolderItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(holder: ChoicesFolderHolder, position: Int) {
        holder.bindItem(folders[position], callback)
    }

    fun submit(data: List<Folder>) {
        if (folders.isNotEmpty()) folders.clear()
        folders.addAll(data)
        notifyDataSetChanged()
    }
}