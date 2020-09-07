package com.isanechek.imagehandler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.service.GalleryJobContract
import org.koin.android.ext.android.inject

class MainActivity2 : AppCompatActivity(_layout.main_activity) {

    private val prefManager: PrefManager by inject()
    private val jobService: GalleryJobContract by inject()
    private val controller: NavController by lazy { findNavController(_id.main_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!jobService.serviceIsRun(this)) {
            jobService.scheduleJob(this)
        }

    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

}