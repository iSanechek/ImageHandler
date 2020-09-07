package com.isanechek.imagehandler.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bindTo(data: T, OnClick: (ItemClickCallback<T>) -> Unit)
}