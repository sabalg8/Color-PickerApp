package com.example.colorpick2024
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NewCameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var colorPreview: View
    private lateinit var colorHexCode: TextView
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var isFlashOn = false

    private val handler = Handler(Looper.getMainLooper())
    private var colorUpdateRunnable: Runnable? = null
    private val colorUpdateDelayMillis: Long = 900 // Delay in milliseconds
    private val colorChangeThreshold: Int = 80 // Threshold for color change detection
    private var lastColor: Int? = null
    private var lastUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_camera)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.app_name)

        // Find views
        viewFinder = findViewById(R.id.viewFinder)
        colorPreview = findViewById(R.id.colorPreview)
        colorHexCode = findViewById(R.id.colorHexCode)

        // Check for camera permission and start the camera if granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                MainActivity.REQUEST_CAMERA
            )
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), { image ->
                        handleImageAnalysis(image)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
            } catch (exc: Exception) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleImageAnalysis(image: ImageProxy) {
        // Extract color and timestamp
        val currentColor = extractColorFromCrosshair(image)
        val currentTime = System.currentTimeMillis()

        // Cancel any previously scheduled color update
        colorUpdateRunnable?.let { handler.removeCallbacks(it) }

        // Schedule a new color update if needed
        colorUpdateRunnable = Runnable {
            // Check if the color has changed significantly or the delay has passed
            if (shouldUpdateColor(currentColor, currentTime)) {
                lastColor = currentColor
                lastUpdateTime = currentTime
                updateColorUI(currentColor)
            }
            image.close()
        }
        handler.postDelayed(colorUpdateRunnable!!, colorUpdateDelayMillis)
    }

    private fun extractColorFromCrosshair(image: ImageProxy): Int {
        val bitmap = image.toBitmap()
        val crosshairX = bitmap.width / 2
        val crosshairY = bitmap.height / 2
        return bitmap.getPixel(crosshairX, crosshairY)
    }

    private fun shouldUpdateColor(currentColor: Int, currentTime: Long): Boolean {
        return (lastColor == null || colorDifference(lastColor!!, currentColor) > colorChangeThreshold ||
                currentTime - lastUpdateTime > colorUpdateDelayMillis)
    }

    private fun colorDifference(color1: Int, color2: Int): Int {
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF
        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)
    }

    private fun updateColorUI(color: Int) {
        val hexColor = String.format("#%06X", (0xFFFFFF and color))
        runOnUiThread {
            colorPreview.setBackgroundColor(color)
            colorHexCode.text = hexColor
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_camera, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pick_image -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, MainActivity.REQUEST_GALLERY)
                true
            }
            R.id.action_toggle_flash -> {
                toggleFlashlight()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleFlashlight() {
        cameraInfo?.let {
            if (it.hasFlashUnit()) {
                cameraControl?.enableTorch(!isFlashOn)
                isFlashOn = !isFlashOn
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == MainActivity.REQUEST_GALLERY) {
            val selectedImageUri = data?.data
            val intent = Intent(this, PickedImageActivity::class.java)
            intent.putExtra("imageUri", selectedImageUri.toString())
            startActivity(intent)
        }
    }
}
