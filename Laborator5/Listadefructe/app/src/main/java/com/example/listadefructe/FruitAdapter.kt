package com.example.listadefructe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapterul care gestionează lista de fructe în RecyclerView.
 *
 * @param fruitList Lista de obiecte Fruit pe care o va afișa adapterul.
 */
class FruitAdapter(private val fruitList: List<Fruit>) :
    RecyclerView.Adapter<FruitAdapter.FruitViewHolder>() {

    // 1. Definim ViewHolder-ul (care păstrează referințele la componentele din item_fruit.xml)
    class FruitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.fruitNameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.fruitDescriptionTextView)
    }

    // 2. onCreateViewHolder: Creează și returnează un nou ViewHolder (inflating the layout)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FruitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fruit, parent, false)
        return FruitViewHolder(view)
    }

    // 3. onBindViewHolder: Conectează datele dintr-un anumit Fruit (după poziție) la ViewHolder
    override fun onBindViewHolder(holder: FruitViewHolder, position: Int) {
        val currentFruit = fruitList[position]

        // Setează datele în componentele vizuale
        holder.nameTextView.text = currentFruit.name
        holder.descriptionTextView.text = currentFruit.description

        // Puteți adăuga un click listener aici
        holder.itemView.setOnClickListener {
            // Exemplu: afișează un toast când se apasă pe un fruct
            // Toast.makeText(holder.itemView.context, "Ați selectat ${currentFruit.name}", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. getItemCount: Returnează numărul total de elemente din listă
    override fun getItemCount(): Int {
        return fruitList.size
    }
}