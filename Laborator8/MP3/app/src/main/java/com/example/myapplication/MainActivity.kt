package com.example.myapplication

import android.util.Log
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.view.View

class MainActivity : View.OnClickListener, AppCompatActivity() {

    // Obiecte pentru butoanele Cronometru
    var button1: Button? = null // Start Ringtone
    var button2: Button? = null // Stop Ringtone

    // Obiecte pentru butoanele MP3
    var button3: Button? = null // Start/Resume MP3
    var button4: Button? = null // Pauză MP3
    var button5: Button? = null // Stop MP3

    val tag_name: String = "Android Services: "

    // Constante pentru Acțiunile MP3, folosite pentru a comunica cu SoundService
    companion object {
        const val ACTION_PLAY = "com.example.myapplication.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.myapplication.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.myapplication.ACTION_STOP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inițializare și setare Listeners pentru toate butoanele

        // Cronometru (Ringtone)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button1?.setOnClickListener(this)
        button2?.setOnClickListener(this)

        // MP3
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button3?.setOnClickListener(this)
        button4?.setOnClickListener(this)
        button5?.setOnClickListener(this)
    }

    override fun onClick(current_view: View) {
        val intentCustom = Intent(this, CustomService::class.java)
        val intentSound = Intent(this, SoundService::class.java)

        when (current_view.id) {
            // Butoane Cronometru (Ringtone)
            R.id.button1 -> {
                startService(intentCustom)
                Log.d(tag_name, "CustomService Start Requested")
            }
            R.id.button2 -> {
                stopService(intentCustom)
                Log.d(tag_name, "CustomService Stop Requested")
            }

            // Butoane MP3 (Comenzi către SoundService)
            R.id.button3 -> {
                intentSound.action = ACTION_PLAY
                startService(intentSound)
                Log.d(tag_name, "SoundService PLAY/RESUME Requested")
            }
            R.id.button4 -> {
                intentSound.action = ACTION_PAUSE
                startService(intentSound)
                Log.d(tag_name, "SoundService PAUSE Requested")
            }
            R.id.button5 -> {
                intentSound.action = ACTION_STOP
                stopService(intentSound) // stopService oprește Serviciul
                Log.d(tag_name, "SoundService STOP Requested")
            }
        }
    }
}