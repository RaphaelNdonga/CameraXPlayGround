package com.cameraxplayground

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.cameraxplayground.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateScreenFlash()
            }
        }
        binding.switchCam.setOnClickListener {
            switchCamera()
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateScreenFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50L)
        }, 100L)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(baseContext, "Camera Permissions Denied", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun getOutputDirectory(): File {
        val mediaDirs = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDirs != null && mediaDirs.exists()) mediaDirs else filesDir
    }

    private fun startCamera() {
        //Add listener ensures that we have obtained the camera successfully and is now available for use
        cameraProviderFuture.addListener(Runnable {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.d(TAG, "Use case binding failed. $exc")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchCamera() {
        if (hasBackCamera() && hasFrontCamera()) {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
        } else {
            Toast.makeText(this, "The camera has not been found", Toast.LENGTH_LONG).show()
        }
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
                    + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.i(TAG, "The file has been saved in ${photoFile.toUri().toString()}")
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(baseContext, "An error has occurred", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "The Exception is $exception")
                }

            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBackCamera(): Boolean {
        val cameraProvider = cameraProviderFuture.get()
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    private fun hasFrontCamera(): Boolean {
        val cameraProvider = cameraProviderFuture.get()
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}