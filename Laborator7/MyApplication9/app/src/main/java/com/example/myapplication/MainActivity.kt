package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Declararea binding-ului (View Binding)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflarea layout-ului folosind View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Setarea listener-ului pentru butonul de start
        binding.loadImagesButton.setOnClickListener {
            // 3. Crearea și pornirea Intent-ului către ImagesActivity
            val intent = Intent(this, ImagesActivity::class.java)
            startActivity(intent)
        }
    }
}