package com.example.imagines_thread

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView
    private lateinit var textStatus: TextView
    private lateinit var buttonStart: Button
    private lateinit var imageViewGallery: ImageView

    private var currentTask: ImageGalleryTask? = null

    // Clasă ajutătoare cu structură îmbunătățită
    private data class GalleryItem(
        val imageResourceId: Int,
        val title: String,          // Denumirea cifrei/literei
        val funFact: String,        // Descrierea scurtă/
        val index: Int              // Indicele pentru progres
    )

    private val galleryItems: List<GalleryItem>
        get() {
            val items = mutableListOf<GalleryItem>()

            // Datele Statice pentru Curiozități
            val facts = listOf(
                "Zero este un număr par.",
                "Unu este primul număr prim.",
                "Doi este singurul număr prim par.",
                "Trei este un număr triunghiular.",
                "Patru este un număr pătrat perfect.",
                "Cinci este numărul degete de la o mână.",
                "Șase este un număr perfect.",
                "Șapte este considerat număr norocos.",
                "Opt este un număr cubic perfect.",
                "Nouă este cel mai mare număr dintr-o cifră.",
                "A este prima literă din alfabet.",
                "B este folosit des în notația muzicală."
            )

            // Cifrele 0-9
            for (i in 0..9) {
                val resourceId = resources.getIdentifier("img$i", "drawable", packageName)
                items.add(GalleryItem(
                    resourceId,
                    "Cifra $i",
                    facts[i],
                    i
                ))
            }

            // Literele A și B
            val resourceIdA = resources.getIdentifier("imga", "drawable", packageName)
            items.add(GalleryItem(
                resourceIdA,
                "Litera A",
                facts[10], // Curiozitatea pentru A
                10
            ))

            val resourceIdB = resources.getIdentifier("imgb", "drawable", packageName)
            items.add(GalleryItem(
                resourceIdB,
                "Litera B",
                facts[11], // Curiozitatea pentru B
                11
            ))

            return items
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inițializarea noilor elemente UI
        textTitle = findViewById(R.id.text_title)
        textDescription = findViewById(R.id.text_description)
        textStatus = findViewById(R.id.text_status) // Fostul textResult
        buttonStart = findViewById(R.id.button_start)
        imageViewGallery = findViewById(R.id.image_view_gallery)

        // Setează starea inițială a textului
        textTitle.text = "Gata de pornire"


        buttonStart.setOnClickListener {
            if (currentTask?.status == AsyncTask.Status.RUNNING) {
                // Dacă sarcina rulează, anulează (Stop)
                currentTask?.cancel(true)
            } else {
                // Dacă sarcina nu rulează, pornește (Start)
                startGallery()
            }
        }
    }

    private fun startGallery() {
        val gallery = galleryItems
        currentTask = ImageGalleryTask()
        currentTask!!.execute(gallery) // Trimitem lista ca parametru la AsyncTask
    }

    private inner class ImageGalleryTask : AsyncTask<List<GalleryItem>, GalleryItem, String>() {

        private var totalItems = 0

        override fun onPreExecute() {
            buttonStart.text = "STOP"
            textStatus.text = "Încărcare: Pregătirea galeriei..."
        }

        override fun doInBackground(vararg params: List<GalleryItem>?): String {
            val gallery = params[0] ?: return "EROARE: Lista de imagini lipsește."
            totalItems = gallery.size

            for (item in gallery) {
                if (isCancelled) return "Sarcina a fost anulată de utilizator."

                try {
                    Thread.sleep(2000) // Pauză de 2 secunde

                    publishProgress(item) // Publică progresul

                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return "Sarcina a fost întreruptă."
                }
            }
            return "Galeria s-a terminat!"
        }

        override fun onProgressUpdate(vararg values: GalleryItem) {
            val item = values.firstOrNull() ?: return

            // 1. Actualizează Imaginea
            if (item.imageResourceId != 0) {
                imageViewGallery.setImageResource(item.imageResourceId)
            } else {
                imageViewGallery.setImageDrawable(null)
                textStatus.text = "EROARE: Resursă invalidă la ${item.title}!"
            }

            // 2. Actualizează Titlul (Cifra/Litera)
            textTitle.text = item.title

            // 3. Actualizează Descrierea (Curiozitatea)
            textDescription.text = item.funFact

            // 4. Actualizează Statusul
            textStatus.text = "Progres: ${item.index + 1} din $totalItems"
        }

        override fun onPostExecute(result: String) {
            textStatus.text = result
            buttonStart.text = "START"
        }

        override fun onCancelled(result: String?) {
            textStatus.text = result ?: "Galeria a fost oprită."
            buttonStart.text = "START"
            // Resetăm titlul și descrierea la anulare
            textTitle.text = "Oprit"

        }
    }
}