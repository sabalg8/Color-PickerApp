package com.example.colorpick2024

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide

class PickedImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picked_image)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.app_name)

        // Find views
        // Get the ImageView and set the image URI
        val imageView: ImageView = findViewById(R.id.imageView)
        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .into(imageView)
        }

        // Set the ImageView in the CrosshairView
        val crosshairView: CrosshairView = findViewById(R.id.crosshairView)
        crosshairView.setImageView(imageView)

        // Set the Color Info Layout and TextView in the CrosshairView
        val colorInfoLayout: LinearLayout = findViewById(R.id.colorInfoLayout)
        val colorCodeTextView: TextView = findViewById(R.id.colorCodeTextView)
        crosshairView.setColorInfoViews(colorInfoLayout, colorCodeTextView)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_camera -> {
                val intent = Intent(this, NewCameraActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
