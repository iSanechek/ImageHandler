package com.isanechek.imagehandler.ui.ml

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import glimpse.core.crop
import glimpse.core.findCenter
import kotlinx.android.synthetic.main.find_objects_fragment_layout.*

class FindObjectsFragment : Fragment(_layout.find_objects_fragment_layout) {

    private lateinit var detector: FirebaseVisionObjectDetector

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

        fof_choice_btn.onClick {
            askForPermissions(Permission.READ_EXTERNAL_STORAGE) { result ->
                if (result.isAllGranted(Permission.READ_EXTERNAL_STORAGE)) {
                    Intent().run {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(this, "Select picture"),
                            202
                        )
                    }
                } else {
                    debugLog { "PERMISSION FUCK YOU" }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 202 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                bindUi(uri)
//                testCroup(uri)
            } else debugLog { "URI IS NULL" }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }

    private fun testCroup(uri: Uri) {
        fof_iv.setImageURI(uri)

        val bitmap = fof_iv.drawable.toBitmap()
        val(x, y) = bitmap.findCenter()
        val crop = bitmap.crop(x, y, 1080, 1080)
        fof_iv.setImageBitmap(crop)
    }

    private fun bindUi(uri: Uri) {
        fof_iv.setImageURI(uri)
        val image = FirebaseVisionImage.fromFilePath(requireContext(), uri)
        detector.processImage(image).addOnSuccessListener { objects ->
            val dw = DrawingView(requireContext(), objects)
            val bitmap = fof_iv.drawable.toBitmap()
            val i = bitmap.copy(bitmap.config, true)
            dw.draw(Canvas(i))
            fof_iv.post {
                fof_iv.setImageBitmap(i)
            }
        }.addOnFailureListener { error ->
          debugLog { "ERROR FIND OBJECT ${error.message}" }
        }
    }

}