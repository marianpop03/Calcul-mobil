package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), ProductAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<Product>
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inițializăm RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Inițializăm lista de produse
        productList = ArrayList()
        
        productList.add(Product(1, "Laptop Gaming", "Laptop puternic cu procesor i7 și placă video RTX 3070.", 5999.99, R.drawable.laptop))
        productList.add(Product(2, "Smartphone Ultra", "Telefon de ultimă generație cu cameră de 108MP.", 3499.50, R.drawable.smartphone))
        productList.add(Product(3, "Căști Wireless", "Căști over-ear cu anulare activă a zgomotului.", 799.00, R.drawable.headphones))
        productList.add(Product(4, "Smartwatch Sport", "Ceas inteligent cu monitorizare cardiacă și GPS.", 1200.00, R.drawable.smartwatch))
        productList.add(Product(5, "Tastatură Mecanică", "Tastatură RGB cu switch-uri mecanice silențioase.", 450.00, R.drawable.keyboard))


        // Setăm adapterul și ascultătorul de click
        productAdapter = ProductAdapter(productList, this)
        recyclerView.adapter = productAdapter
    }

    // Implementarea metodei din interfața OnItemClickListener
    override fun onItemClick(product: Product) {
        // Când se dă click pe un produs, deschidem DetailActivity
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("selected_product", product) // Trimitem obiectul Product
        startActivity(intent)
    }
}