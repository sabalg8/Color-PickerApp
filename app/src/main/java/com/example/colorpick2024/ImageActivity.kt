package com.example.colorpick2024

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.colorpick2024.R

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val imageUri = intent.getStringExtra("imageUri")
        val imageView = findViewById<ImageView>(R.id.imageView)

        if (imageUri != null) {
            imageView.setImageURI(Uri.parse(imageUri))
        }
    }
}