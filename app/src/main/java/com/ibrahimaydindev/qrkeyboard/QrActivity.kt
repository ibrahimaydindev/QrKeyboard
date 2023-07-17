package com.ibrahimaydindev.qrkeyboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.ibrahimaydindev.qrkeyboard.databinding.ActivityQrBinding
import java.io.IOException


class QrActivity : AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""

    private lateinit var binding: ActivityQrBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }
        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this, R.anim.scanner_animation)
        binding.barcodeLine.startAnimation(aniSlide)
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(this@QrActivity, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue
                    runOnUiThread {
                        cameraSource.stop()
                        binding.cameraSurfaceView.visibility = View.GONE
                        binding.barcodeLine.clearAnimation()
                        binding.barcodeLine.visibility = View.GONE

                        val intent = Intent()
                        intent.putExtra("scanned_value", scannedValue)
                        setResult(Activity.RESULT_OK, intent)
                        finish()

                    }
                } else {
                    runOnUiThread {
                        Thread.sleep(2000)
                        Toast.makeText(this@QrActivity, " Please Show Barcode", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                askForCameraPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_REQUEST_CODE && resultCode == RESULT_OK) {
            val qrResult: String? = data?.getStringExtra("scanned_value")
            if (!qrResult.isNullOrEmpty()) {
                var editext = findViewById<EditText>(R.id.ediTtext)
                editext.setText(qrResult)
                //This code solution but it doesn't work
                //val ic: InputConnection = getCurrentInputConnection();
                //ic.commitText(qrResult, 1);
            }
        }
    }

    companion object {
        const val QR_REQUEST_CODE = 100
    }
}