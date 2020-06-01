package com.isanechek.imagehandler.ui.ml

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRegion
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.isanechek.imagehandler._layout
import com.isanechek.imagehandler.debugLog
import com.isanechek.imagehandler.onClick
import com.isanechek.imagehandler.utils.FileUtils
import glimpse.core.crop
import glimpse.core.findCenter
import kotlinx.android.synthetic.main.find_objects_fragment_layout.*

class FindObjectsFragment : Fragment(_layout.find_objects_fragment_layout) {

    private lateinit var detector: FirebaseVisionObjectDetector

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
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

    private fun bindUi(uri: Uri) {
        fof_iv.setImageURI(uri)
        val image = FirebaseVisionImage.fromFilePath(requireContext(), uri)
        detector.processImage(image).addOnSuccessListener { objects ->

            val realPath = FileUtils.getPath(requireContext(), uri)
            debugLog { "REAL PATH $realPath" }
            val box = objects[0].boundingBox
            val decoder = BitmapRegionDecoder.newInstance(realPath, false)
            val opts = BitmapFactory.Options()
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888
            val result = decoder.decodeRegion(box, opts)
            text(result)
            fof_result.post {
                fof_result.setImageBitmap(result)
            }

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

    private fun text(bitmap: Bitmap) {
        debugLog { "W ${bitmap.width} & H ${bitmap.height}" }
        debugLog { "BOOM" }

        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        detector.processImage(image).addOnSuccessListener { text ->
            debugLog { "Find Text ${text.text}" }
        }.addOnFailureListener { error ->
            debugLog { "Find Error ${error.message}" }
        }
    }
}