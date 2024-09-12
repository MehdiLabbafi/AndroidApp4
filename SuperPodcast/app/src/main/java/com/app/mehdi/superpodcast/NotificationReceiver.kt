package com.app.mehdi.superpodcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "PAUSE_ACTION" -> {
                val mediaPlayer = MainActivity.mediaPlayer
                if (mediaPlayer != null && mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    Toast.makeText(context, "Playback Paused", Toast.LENGTH_SHORT).show()
                }
            }
            "PLAY_ACTION" -> {
                val mediaPlayer = MainActivity.mediaPlayer
                if (mediaPlayer != null && !mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    Toast.makeText(context, "Playback Resumed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
