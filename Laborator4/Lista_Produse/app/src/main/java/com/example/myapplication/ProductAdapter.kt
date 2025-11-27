package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val productList: List<Product>,
    private val listener: OnItemClickListener // Interfața pentru click-uri
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Interfață pentru a gestiona click-urile pe elementele listei
    interface OnItemClickListener {
        fun onItemClick(product: Product)
    }

    // ViewHolder-ul, similar cu cel din exemplul tau
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(productList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = productList[position]
        holder.productImageView.setImageResource(currentProduct.imageResId)
        holder.productNameTextView.text = currentProduct.name
        holder.productPriceTextView.text = "Preț: ${String.format("%.2f", currentProduct.price)} RON"
    }

    override fun getItemCount() = productList.size
}