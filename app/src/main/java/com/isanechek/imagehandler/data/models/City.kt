package com.isanechek.imagehandler.data.models

data class City(val id: String, val name: String, val isSelected: Boolean, val overlayPath: String) {

    companion object {
        fun empty() = City("", "", false, "")
    }
}