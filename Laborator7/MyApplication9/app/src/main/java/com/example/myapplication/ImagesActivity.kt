package com.example.myapplication


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityImagesBinding
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

class ImagesActivity : AppCompatActivity() {

    // Generează lista pentru 9 imagini (img0.jpg ... img8.jpg)
    private val imageUrls = List(9) { "https://cti.ubm.ro/cmo/digits/img${it}.jpg" }
    private val imageAdapter = ImageAdapter()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var binding: ActivityImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setează layout-ul pe GRILĂ cu 3 coloane
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = imageAdapter

        loadImages()
    }

    private fun loadImages() {
        coroutineScope.launch {
            val loadingTime = measureTimeMillis {
                val images = imageUrls.map { url ->
                    async { downloadImage(url) } // Descarcă asincron fiecare imagine
                }.awaitAll()

                withContext(Dispatchers.Main) {
                    imageAdapter.submitList(images)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ImagesActivity, "Timp: $loadingTime ms", Toast.LENGTH_LONG).show()
            }
        }
    }



    private suspend fun downloadImage(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            // Verifică dacă răspunsul este OK (Cod 200)
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // Citim întregul flux de date într-un ByteArray
                val imageBytes = connection.inputStream.readBytes()
                // Decodează ByteArray-ul în Bitmap
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } else {
                // Log eroarea de răspuns (ex: 404, 500)
                Log.e("DOWNLOAD_DEBUG", "Eroare HTTP: Cod ${connection.responseCode} pentru $url")
                return null
            }
        } catch (e: Exception) {
            // Log excepția (UnknownHost, SecurityException etc.)
            Log.e("DOWNLOAD_DEBUG", "Eroare la descarcare $url: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Curăță corutinele
    }
}