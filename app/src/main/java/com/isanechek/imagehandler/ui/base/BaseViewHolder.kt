package com.isanechek.imagehandler.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

    abstract fun bindTo(data: T, OnClick: (ItemClickCallback<T>) -> Unit)

    override val containerView: View? = itemView
}