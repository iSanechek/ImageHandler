package com.isanechek.imagehandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.isanechek.imagehandler.data.local.system.PrefManager
import com.isanechek.imagehandler.service.GalleryJobContract
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(_layout.main_activity) {

    private val prefManager: PrefManager by inject()
    private val jobService: GalleryJobContract by inject()
    private val controller: NavController by lazy { findNavController(_id.main_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // start service
        startService()

        // navigation
        val actionId = if (prefManager.isFirstStart()) _id.go_to_select_from_splash else _id.go_to_dashboard_from_splash
        Handler().postDelayed({
            controller.navigate(
                actionId,
                null,
                NavOptions.Builder()
                    .setEnterAnim(_anim.slide_up_anim)
                    .setExitAnim(_anim.alpha_out_anim)
                    .setPopExitAnim(_anim.alpha_out_anim)
                    .setPopEnterAnim(_anim.slide_up_anim)
                    .setPopUpTo(_id.splash_screen, true)
                    .build()
            )
        }, 2500)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    private fun startService() {
        if (!jobService.serviceIsRun(this)) {
            jobService.scheduleJob(this)
        }
    }
}