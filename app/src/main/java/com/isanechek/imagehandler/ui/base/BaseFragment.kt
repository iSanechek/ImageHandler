package com.isanechek.imagehandler.ui.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUi(savedInstanceState)
    }

    abstract fun bindUi(savedInstanceState: Bundle?)

    fun showWarningDialog(title: String, message: String, callback: (Boolean) -> Unit) {
        MaterialDialog(requireContext()).show {
            title(text = title)
            message(text = message)
            cancelable(false)
            cancelOnTouchOutside(false)
            positiveButton(text = "ok") { callback.invoke(true) }
            negativeButton(text = "cancel") { callback.invoke(false) }
        }
    }

    fun showSnack(parentView: CoordinatorLayout, message: String) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackWithAction(
        parentView: CoordinatorLayout,
        message: String,
        undoTitle: String = "Undo",
        action: () -> Unit
    ) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).setAction(
            undoTitle
        ) { action.invoke() }.show()
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Activity, message: String, style: String = "info") {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        val motionStyle = when (style) {
//            "info" -> MotionToast.TOAST_INFO
//            "delete" -> MotionToast.TOAST_DELETE
//            else -> MotionToast.TOAST_WARNING
//        }


//        MotionToast.darkToast(
//            context,
//            message,
//            motionStyle,
//            MotionToast.GRAVITY_BOTTOM,
//            MotionToast.SHORT_DURATION,
//            ResourcesCompat.getFont(
//                requireContext(),
//                R.font.helvetica_regular
//            )
//        )
    }
}