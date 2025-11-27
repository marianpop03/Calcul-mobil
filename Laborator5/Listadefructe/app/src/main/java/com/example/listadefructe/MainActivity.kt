// MainActivity.kt




package com.example.listadefructe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fruitAdapter: FruitAdapter
    private lateinit var fruitList: List<Fruit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Generare date (lista de fructe)
        fruitList = generateDummyFruitList()

        // 2. Inițializare RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFruits)

        // 3. Setare LayoutManager (specifică cum sunt aranjate elementele: liniar, grid etc.)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 4. Inițializare și atașare Adapter
        fruitAdapter = FruitAdapter(fruitList)
        recyclerView.adapter = fruitAdapter
    }

    /**
     * Funcție helper pentru a genera o listă de fructe de test.
     */
    private fun generateDummyFruitList(): List<Fruit> {
        return listOf(
            Fruit("Măr", "Fruct crocant, bogat în fibre și vitamină C."),
            Fruit("Banană", "Excelentă sursă de potasiu, perfectă pentru energie rapidă."),
            Fruit("Portocală", "Citrice suculentă, renumită pentru conținutul său de Vitamina C."),
            Fruit("Căpșună", "Fruct roșu, dulce-acrișor, ideal pentru deserturi."),
            Fruit("Kiwi", "Fruct exotic cu interior verde intens și semințe negre."),
            Fruit("Ananas", "Fruct tropical, ideal pentru smoothie-uri și cocktail-uri."),
            Fruit("Mango", "Regele fructelor, foarte dulce și aromat.")
            // Adăugați mai multe fructe după dorință
        )
    }
}