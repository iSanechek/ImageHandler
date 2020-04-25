package com.isanechek.imagehandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.isanechek.imagehandler.service.GalleryJobContract
import com.isanechek.imagehandler.ui.main.MainFragment
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    val jobService: GalleryJobContract by inject()

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        startService()
//        Handler().postDelayed({
//            controller.navigate(
//                _id.go_from_splash_to_handler,
//                null,
//                NavOptions.Builder()
//                    .setExitAnim(_anim.alpha_out_anim)
//                    .setEnterAnim(_anim.alpha_in_anim)
//                    .setPopEnterAnim(_anim.alpha_in_anim)
//                    .setPopExitAnim(_anim.alpha_out_anim)
//                    .setPopUpTo(_id.splash_screen, true)
//                    .build()
//            )
//        }, 1000)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    private fun startService() {

        if (!jobService.serviceIsRun(this)) {
            jobService.scheduleJob(this)
            d { "Run service" }
        } else d { "Service is run" }
    }
}