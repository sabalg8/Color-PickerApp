package com.example.colorpick2024

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.colorpick2024.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CAMERA = 1001
        const val REQUEST_GALLERY = 1002
        private const val PREFS_NAME = "ColorPickerPrefs"
        private const val KEY_DIALOG_SHOWN = "dialog_shown"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // If User Opens the app for first time,
        // then we show a dialogue either to open a camera
        // or pick a image from gallery.
        // else start a cameraActivity
        if (isFirstTimeLaunch()) {
            showCameraAccessDialog()
        } else {
            startCameraActivity()
        }
    }

    private fun isFirstTimeLaunch(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val dialogShown = prefs.getBoolean(KEY_DIALOG_SHOWN, false)
        if (!dialogShown) {
            // Mark dialog as shown
            prefs.edit().putBoolean(KEY_DIALOG_SHOWN, true).apply()
        }
        return !dialogShown
    }

    private fun showCameraAccessDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_camera_access, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        // When "GRANT ACCESS TO CAMERA" is clicked
        dialogView.findViewById<Button>(R.id.btn_grant_camera_access).setOnClickListener {
            alertDialog.dismiss()
            openCamera()
        }

        // When "OPEN IMAGE" is clicked
        dialogView.findViewById<Button>(R.id.btn_open_image).setOnClickListener {
            alertDialog.dismiss()
            openGallery()
        }
    }

    private fun openCamera() {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the camera permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        } else {
            // If permission is granted, start NewCameraActivity
            startCameraActivity()
        }
    }

    private fun openGallery() {
        // Open the gallery picker
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun startCameraActivity() {
        val intent = Intent(this, NewCameraActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish MainActivity if you don't want it in the back stack
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If camera permission is granted, start NewCameraActivity
            startCameraActivity()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            // If an image is picked from the gallery, start PickedImageActivity
            val selectedImageUri = data?.data
            val intent = Intent(this, PickedImageActivity::class.java)
            intent.putExtra("imageUri", selectedImageUri.toString())
            startActivity(intent)
        }
    }
}
