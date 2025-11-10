package com.example.gestordearchivos

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class TextPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_player)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val filePath = intent.getStringExtra("file_path")
        if (filePath != null) {
            val file = File(filePath)
            toolbar.title = file.name
            val textContentTextView: TextView = findViewById(R.id.textContentTextView)
            textContentTextView.text = file.readText()
        }
    }
}