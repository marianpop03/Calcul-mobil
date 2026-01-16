package com.example.p2p_lending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val loanList = ArrayList<Loan>()
    private lateinit var adapter: LoanAdapter

    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("username", "Anonim") ?: "Anonim"


        val recyclerView = findViewById<RecyclerView>(R.id.rvLoans)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LoanAdapter(loanList, currentUser)
        recyclerView.adapter = adapter


        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            showAddLoanDialog(currentUser)
        }


        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)
        btnProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val btnChat = findViewById<Button>(R.id.btnGoToChat)

        btnChat.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
        getLoansFromFirebase()
    }

    private fun showAddLoanDialog(currentUser: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_loan, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etInterest = dialogView.findViewById<EditText>(R.id.etInterest) // NOU
        val etPurpose = dialogView.findViewById<EditText>(R.id.etPurpose)

        etName.setText(currentUser)
        etName.isEnabled = false

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Cere un împrumut")
            .setPositiveButton("Salvează") { _, _ ->
                val amountText = etAmount.text.toString()
                val interestText = etInterest.text.toString()
                val purpose = etPurpose.text.toString()

                if (amountText.isNotEmpty() && interestText.isNotEmpty()) {

                    val interest = interestText.toDouble()
                    saveLoanToFirebase(currentUser, amountText.toDouble(), interest, purpose)
                } else {
                    Toast.makeText(this, "Completează toate datele!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun saveLoanToFirebase(name: String,interest: Double, amount: Double, purpose: String) {
        val database = FirebaseDatabase.getInstance(databaseUrl)
        val myRef = database.getReference("loans")


        val newLoan = Loan(
            borrowerName = name,
            amount = amount,
            interestRate = interest,
            purpose = purpose

        )

        myRef.push().setValue(newLoan)
            .addOnSuccessListener {
                Toast.makeText(this, "Cerere trimisă!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Eroare la trimitere", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLoansFromFirebase() {
        val database = FirebaseDatabase.getInstance(databaseUrl)
        val myRef = database.getReference("loans")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loanList.clear()
                for (postSnapshot in snapshot.children) {
                    val loan = postSnapshot.getValue(Loan::class.java)
                    if (loan != null) {

                        loan.id = postSnapshot.key ?: ""
                        loanList.add(loan)
                    }
                }
                loanList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Eroare", error.toException())
            }
        })
    }
}