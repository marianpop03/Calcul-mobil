package com.example.myapplication

import java.io.Serializable // Necesara pentru a trimite obiectul prin Intent

data class Product(
    val id: Int, // Un ID unic pentru produs
    val name: String,
    val details: String,
    val price: Double,
    val imageResId: Int // ID-ul resursei drawable pentru imagine (ex: R.drawable.produs1)
) : Serializable // Implementam Serializable pentru a putea pasa obiectul intre activitati