package com.example.myapplication

import android.content.Intent
import android.app.Service
import android.os.IBinder
import android.media.MediaPlayer
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class CustomService : Service() {
    private lateinit var ringtone_player: MediaPlayer
    private var show_text = ""

    override fun onStartCommand(intent: Intent, flags: Int, Id: Int): Int {
        val ringtone = Settings.System.DEFAULT_RINGTONE_URI

        // Se poate folosi try-catch pentru a gestiona posibile erori de inițializare
        ringtone_player = MediaPlayer.create(this, ringtone)

        show_text = "Serviciu Cronometru START (Ringtone Redă)"
        ringtone_player.isLooping = true
        ringtone_player.start()
        Toast.makeText(this, show_text, Toast.LENGTH_SHORT).show()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        show_text = "Serviciu Cronometru STOP"
        if (this::ringtone_player.isInitialized) {
            ringtone_player.stop()
            ringtone_player.release()
        }
        Toast.makeText(this, show_text, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}