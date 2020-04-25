package com.isanechek.imagehandler.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.isanechek.imagehandler.R
import com.isanechek.imagehandler.d

class CustomSpinner : AppCompatAutoCompleteTextView {


    @SuppressLint("PrivateResource")
    constructor(context: Context, attrs: AttributeSet, defStyle:Int) : super(context, attrs, R.style.Base_Widget_AppCompat_Spinner_Underlined) {
        init(attrs,R.style.Base_Widget_AppCompat_Spinner_Underlined)
    }

    @SuppressLint("PrivateResource")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, R.style.Base_Widget_AppCompat_Spinner_Underlined) {
        init(attrs, R.style.Base_Widget_AppCompat_Spinner_Underlined)
    }

    @SuppressLint("PrivateResource")
    constructor(context: Context) : super(context) {
        init(null, R.style.Base_Widget_AppCompat_Spinner_Underlined)
    }

    private fun init(attrs: AttributeSet?, defStyle:Int) {
        isFocusable = false
        isFocusableInTouchMode = false
        inputType = 0
        this.setOnClickListener {
            d { "click view" }
            this.showDropDown()
        }
    }
}