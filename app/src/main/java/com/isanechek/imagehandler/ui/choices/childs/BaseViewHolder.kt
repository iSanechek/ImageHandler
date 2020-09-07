package com.isanechek.imagehandler.ui.choices.childs

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>
    (containerView: View) : RecyclerView.ViewHolder(containerView),
    LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry
        get() = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    fun onAppear() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onDisappear() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    abstract fun bindItem(item: T, callback: (T) -> Unit)
}