package com.example.p2p_lending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.p2p_lending.ui.login.LoginActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app"
    private val COMPOUND_INTERVAL = 30000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvBalance = findViewById<TextView>(R.id.tvBalance)
        val tvInvested = findViewById<TextView>(R.id.tvInvested)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeposit = findViewById<Button>(R.id.btnDeposit)
        val btnWithdraw = findViewById<Button>(R.id.btnWithdraw)

        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "") ?: ""
        tvName.text = username
        val btnCollect = findViewById<Button>(R.id.btnCollect)
        val tvPending = findViewById<TextView>(R.id.tvPending)

        val database = FirebaseDatabase.getInstance(databaseUrl)
        val userRef = database.getReference("users").child(username)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    tvBalance.text = "${String.format("%.2f", user.balance)} RON"
                    tvInvested.text = "${String.format("%.2f", user.totalInvested)} RON"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        btnDeposit.setOnClickListener {
            showTransactionDialog("Depune Bani", true)
        }

        btnWithdraw.setOnClickListener {
            showTransactionDialog("Retrage Bani", false)
        }
        btnCollect.setOnClickListener {
            checkAndCollectInterests()
        }
        findViewById<Button>(R.id.btnOpenChart).setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }
        updatePendingInfo(tvPending)

        btnLogout.setOnClickListener {
            // Șterge sesiunea
            sharedPref.edit().clear().apply()
            // Merge la Login
            val intent = Intent(this, LoginActivity::class.java)
            // Goleste stiva de activități
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun showTransactionDialog(title: String, isDeposit: Boolean) {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Suma (RON)"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Confirmă") { _, _ ->
                val amountText = input.text.toString()
                if (amountText.isNotEmpty()) {
                    val amount = amountText.toDouble()
                    updateBalance(amount, isDeposit)
                }
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun updateBalance(amount: Double, isDeposit: Boolean) {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "") ?: ""

        val userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(username)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    var newBalance = user.balance

                    if (isDeposit) {
                        newBalance += amount
                        android.widget.Toast.makeText(applicationContext, "Ai depus $amount RON", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        if (user.balance >= amount) {
                            newBalance -= amount
                            android.widget.Toast.makeText(applicationContext, "Ai retras $amount RON", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(applicationContext, "Fonduri insuficiente!", android.widget.Toast.LENGTH_SHORT).show()
                            return
                        }
                    }

                    userRef.child("balance").setValue(newBalance)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun checkAndCollectInterests() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "") ?: ""
        val database = FirebaseDatabase.getInstance(databaseUrl)
        val invRef = database.getReference("user_investments").child(username)

        invRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPayout = 0.0
                var investmentsCollectedCount = 0

                for (invSnapshot in snapshot.children) {
                    val inv = invSnapshot.getValue(Investment::class.java)

                    if (inv != null && !inv.claimed) {
                        val currentTime = System.currentTimeMillis()
                        val timePassed = currentTime - inv.timestamp


                        val intervalsPassed = (timePassed / COMPOUND_INTERVAL).toInt()

                        if (intervalsPassed >= 1) {
                            // FORMULA DE DOBÂNDĂ COMPUSĂ
                            // Suma Finală = Suma Inițială * (1 + rata/100) ^ intervale
                            val rateDecimal = inv.interestRate / 100.0
                            val multiplier = Math.pow((1 + rateDecimal), intervalsPassed.toDouble())
                            val finalAmount = inv.amount * multiplier

                            totalPayout += finalAmount
                            investmentsCollectedCount++


                            invRef.child(inv.id).child("claimed").setValue(true)
                        }
                    }
                }

                if (totalPayout > 0) {

                    addMoneyToUser(username, totalPayout)

                    val msg = "Ai colectat $investmentsCollectedCount investiții!\nTotal încasat: ${String.format("%.2f", totalPayout)} RON"
                    android.widget.Toast.makeText(applicationContext, msg, android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(applicationContext, "Încă se acumulează dobândă... Mai așteaptă!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addMoneyToUser(username: String, amount: Double) {
        val userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    userRef.child("balance").setValue(user.balance + amount)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun updatePendingInfo(tv: TextView) {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "") ?: ""
        val invRef = FirebaseDatabase.getInstance(databaseUrl).getReference("user_investments").child(username)

        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                invRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalPotentialValue = 0.0
                        var initialInvestment = 0.0

                        for (child in snapshot.children) {
                            val inv = child.getValue(Investment::class.java)
                            if (inv != null && !inv.claimed) {
                                val timePassed = System.currentTimeMillis() - inv.timestamp
                                val intervalsPassed = (timePassed / COMPOUND_INTERVAL).toInt()

                                initialInvestment += inv.amount

                                if (intervalsPassed >= 1) {
                                    val rateDecimal = inv.interestRate / 100.0
                                    val multiplier = Math.pow((1 + rateDecimal), intervalsPassed.toDouble())
                                    totalPotentialValue += (inv.amount * multiplier)
                                } else {

                                    totalPotentialValue += inv.amount
                                }
                            }
                        }

                        val profitOnly = totalPotentialValue - initialInvestment


                        if (initialInvestment > 0) {
                            tv.text = "Investiți: ${initialInvestment.toInt()} RON\n" +
                                    "Valoare curentă: ${String.format("%.2f", totalPotentialValue)} RON\n" +
                                    "(Profit acumulat: +${String.format("%.2f", profitOnly)} RON)"
                        } else {
                            tv.text = "Nu ai investiții active."
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }
}