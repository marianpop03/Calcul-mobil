package com.example.p2p_lending

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var currentUser: String
    private lateinit var otherUser: String
    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app" // URL-ul tău

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val tvTitle = findViewById<TextView>(R.id.tvChatTitle)
        val recycler = findViewById<RecyclerView>(R.id.recyclerChat)
        val etMessage = findViewById<EditText>(R.id.etChatMessage)
        val btnSend = findViewById<Button>(R.id.btnSendChat)


        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        currentUser = sharedPref.getString("username", "") ?: ""
        otherUser = intent.getStringExtra("otherUser") ?: ""

        if (currentUser.isEmpty() || otherUser.isEmpty()) {
            Toast.makeText(this, "Eroare utilizatori!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTitle.text = "Chat cu $otherUser"


        adapter = ChatAdapter(currentUser)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter



        val chatId = if (currentUser < otherUser) {
            "${currentUser}_${otherUser}"
        } else {
            "${otherUser}_${currentUser}"
        }

        chatRef = FirebaseDatabase.getInstance(databaseUrl).getReference("chats").child(chatId)


        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ChatMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) list.add(msg)
                }
                adapter.setMessages(list)

                if (list.isNotEmpty()) {
                    recycler.scrollToPosition(list.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })


        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = ChatMessage(currentUser, text, System.currentTimeMillis())
                chatRef.push().setValue(message)
                etMessage.setText("")
            }
        }
    }
}