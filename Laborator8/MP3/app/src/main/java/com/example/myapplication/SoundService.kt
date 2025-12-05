package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.widget.Toast
import android.util.Log

class SoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int = 0

    // Preluăm acțiunile definite în MainActivity
    private val ACTION_PLAY = MainActivity.ACTION_PLAY
    private val ACTION_PAUSE = MainActivity.ACTION_PAUSE
    private val ACTION_STOP = MainActivity.ACTION_STOP

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Logica de bază se execută pe baza acțiunii primite
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_STOP -> handleStop()
            // Dacă serviciul este pornit fără o acțiune specifică (e.g. la prima pornire)
            else -> handlePlay()
        }

        return START_NOT_STICKY // Nu reporni automat dacă este oprit de sistem
    }

    private fun handlePlay() {
        if (mediaPlayer == null) {
            // Inițializare (se întâmplă la prima rulare sau după un STOP complet)
            mediaPlayer = MediaPlayer.create(this, R.raw.example)
            mediaPlayer?.setOnCompletionListener {
                stopSelf() // Se oprește la terminarea melodiei
                Toast.makeText(this, "Redare MP3 terminată", Toast.LENGTH_SHORT).show()
                resumePosition = 0
            }
        }

        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.seekTo(resumePosition) // Reia de la poziția salvată
            mediaPlayer?.start()
            Toast.makeText(this, "Redare MP3 START/RESUME", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePause() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
            resumePosition = mediaPlayer!!.currentPosition // Salvează poziția curentă
            Toast.makeText(this, "Redare MP3 PAUZĂ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleStop() {
        // Logica de STOP se face în onDestroy (la apelul stopService din MainActivity)
        Toast.makeText(this, "Redare MP3 STOP", Toast.LENGTH_SHORT).show()
        stopSelf() // Oprește serviciul, declanșând onDestroy
    }

    override fun onDestroy() {
        super.onDestroy()
        // Eliberarea resurselor este esențială aici
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            resumePosition = 0
        }
        Log.d("SoundService", "Service Destroyed. Resources released.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}