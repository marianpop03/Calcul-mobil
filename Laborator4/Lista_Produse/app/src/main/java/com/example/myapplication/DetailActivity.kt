package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Obținem referințele la elementele UI
        val detailImageView: ImageView = findViewById(R.id.detailImageView)
        val detailNameTextView: TextView = findViewById(R.id.detailNameTextView)
        val detailDetailsTextView: TextView = findViewById(R.id.detailDetailsTextView)
        val detailPriceTextView: TextView = findViewById(R.id.detailPriceTextView)

        // Obținem obiectul Product trimis prin Intent
        val selectedProduct = intent.getSerializableExtra("selected_product") as? Product

        // Verificăm dacă obiectul Product există și afișăm detaliile
        selectedProduct?.let { product ->
            detailImageView.setImageResource(product.imageResId)
            detailNameTextView.text = product.name
            detailDetailsTextView.text = product.details
            detailPriceTextView.text = "Preț: ${String.format("%.2f", product.price)} RON"
        }

        // Adăugăm un buton de back în bara de acțiune (opțional)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = selectedProduct?.name ?: "Detalii Produs"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}