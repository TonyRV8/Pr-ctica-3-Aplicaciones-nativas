package com.example.gestordearchivos

import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class ImagePlayerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private val matrix = Matrix()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_player)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imageView = findViewById(R.id.imageView)
        val filePath = intent.getStringExtra("file_path")

        if (filePath != null) {
            val file = File(filePath)
            toolbar.title = file.name
            Glide.with(this).load(file).into(imageView)
        }

        setupZoomAndPan()
        setupRotationButtons()
    }

    private fun setupZoomAndPan() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
                imageView.scaleX = scaleFactor
                imageView.scaleY = scaleFactor
                return true
            }
        })

        imageView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupRotationButtons() {
        val rotateLeftButton: FloatingActionButton = findViewById(R.id.rotateLeftButton)
        val rotateRightButton: FloatingActionButton = findViewById(R.id.rotateRightButton)

        rotateLeftButton.setOnClickListener {
            imageView.rotation -= 90
        }

        rotateRightButton.setOnClickListener {
            imageView.rotation += 90
        }
    }
}