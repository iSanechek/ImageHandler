package com.isanechek.imagehandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.isanechek.imagehandler.service.GalleryJobContract
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    val jobService: GalleryJobContract by inject()

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
//        startService()

        val actionId = if (false) _id.go_to_handler_from_splash else _id.go_to_select_from_splash

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
        }, 1000)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    private fun startService() {

        if (!jobService.serviceIsRun(this)) {
            jobService.scheduleJob(this)
            debugLog { "Run service" }
        } else debugLog { "Service is run" }
    }
}