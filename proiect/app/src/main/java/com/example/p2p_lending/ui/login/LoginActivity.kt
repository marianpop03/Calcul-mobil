package com.example.p2p_lending.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.p2p_lending.MainActivity
import com.example.p2p_lending.R
import com.example.p2p_lending.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Verifică dacă e deja logat
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getString("username", null) != null) {
            goToMain()
            return
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val database = FirebaseDatabase.getInstance(databaseUrl)
        val usersRef = database.getReference("users")

        // LOGICA DE LOGIN
        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                usersRef.child(user).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dbUser = snapshot.getValue(User::class.java)
                            if (dbUser != null && dbUser.password == pass) {
                                saveSession(user)
                                goToMain()
                            } else {
                                Toast.makeText(applicationContext, "Parolă greșită!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(applicationContext, "Utilizatorul nu există!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }

        // LOGICA DE ÎNREGISTRARE
        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                usersRef.child(user).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(applicationContext, "Nume deja folosit!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Creăm user nou cu 5000 RON
                            val newUser = User(username = user, password = pass, balance = 5000.0, totalInvested = 0.0)
                            usersRef.child(user).setValue(newUser)
                            saveSession(user)
                            goToMain()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun saveSession(username: String) {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("username", username).apply()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}