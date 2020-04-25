package com.isanechek.imagehandler.data.models

data class Folder(val id: String, val name: String, val caverPaths: Set<String>, val modification: Long) {

    companion object {
        fun coverPathsToString(list: Set<String>): String =
            list.filter { it.isNotEmpty() }.joinToString()

        fun stringToCoverPathsList(string: String): Set<String> =
            string.split(",").map { it.trim() }.toSet()
    }
}