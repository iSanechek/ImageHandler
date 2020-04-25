package com.isanechek.imagehandler.ui.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.isanechek.imagehandler.R
import com.isanechek.imagehandler.d
import com.isanechek.imagehandler.data.local.system.gallery.GalleryManager
import com.isanechek.imagehandler.data.models.ChoicesResult
import com.isanechek.imagehandler.data.repositories.ChoicesRepository
import com.isanechek.imagehandler.onClick
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity(R.layout.debug_layout) {

    private val manager: GalleryManager by inject()

    private val choices: ChoicesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(debug_toolbar) {

            title = "Hello"
        }

        lifecycleScope.launchWhenResumed {
            choices.loadFolders(this@DebugActivity).observe(this@DebugActivity, Observer { data ->
                when(data) {
                    is ChoicesResult.Update -> {
                        d { "update" }
                    }
                    is ChoicesResult.Load -> {
                        d { "load" }
                    }
                    is ChoicesResult.Done -> {
                        d { "done" }

                        data.data.forEach { item ->
                            d { "name ${item.name}" }
                            item.caverPaths.forEach { url ->
                                d { "path $url" }
                            }
                        }
                    }
                    is ChoicesResult.Error -> {
                        d { "error ${data.message}" }
                    }
                }
            })
        }


//        lifecycleScope.launchWhenResumed {
//            withContext(Dispatchers.IO) {
//                val result = manager.loadAlbums(this@DebugActivity, 0)
//                d { "Load Images Result Size ${result.size}" }
//                result.forEach { item ->
//                    d { "Image id ${item.id}" }
//                    d { "Image name ${item.name}" }
//                    d { "Image path ${item.path}" }
//                    val images = item.images
//                    d { "Images size ${images.size}" }
//                    d { images.toString() }
//
//                    d { "============================" }
////                    d { "Image date ${item.addDate}" }
//                }
//            }
//        }

        debug_btn.onClick {
            debug_card.isChecked = !debug_card.isChecked
        }
    }
}