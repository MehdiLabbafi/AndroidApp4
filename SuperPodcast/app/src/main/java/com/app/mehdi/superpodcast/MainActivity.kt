package com.app.mehdi.superpodcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mehdi.superpodcast.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    private var currentSongIndex: Int = 0
    private var songList: List<PodcastResult> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Notification permission request for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Create notification channel
        createNotificationChannel()

        // Artist search
        binding.searchButton.setOnClickListener {
            val searchQuery = binding.searchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchPodcasts(searchQuery)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the function of the Play button
        binding.playButton.setOnClickListener {
            if (songList.isNotEmpty()) {
                playSong(songList[currentSongIndex].previewUrl)
                showNotification(songList[currentSongIndex])
            } else {
                Toast.makeText(this, "No song selected.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the function of the Pause button
        binding.pauseButton.setOnClickListener {
            pauseSong()
        }

        // Set the function of the Next button
        binding.nextButton.setOnClickListener {
            nextSong()
        }

        // Set the function of the Previous button
        binding.previousButton.setOnClickListener {
            previousSong()
        }
    }

    private fun searchPodcasts(artistName: String) {
        RetrofitInstance.api.searchByArtist(artistName).enqueue(object : Callback<PodcastResponse> {
            override fun onResponse(call: Call<PodcastResponse>, response: Response<PodcastResponse>) {
                if (response.isSuccessful) {
                    val results = response.body()?.results
                    if (results.isNullOrEmpty()) {
                        Toast.makeText(this@MainActivity, "No songs found for this artist.", Toast.LENGTH_SHORT).show()
                    } else {
                        songList = results
                        currentSongIndex = 0
                        displaySongs(songList)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to retrieve data.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PodcastResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displaySongs(songs: List<PodcastResult>) {
        val adapter = SongAdapter(songs) { song ->
            playSong(song.previewUrl)
            currentSongIndex = songs.indexOf(song)
            showNotification(song)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun playSong(previewUrl: String?) {
        if (previewUrl != null) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                mediaPlayer?.reset()
            }

            try {
                mediaPlayer?.setDataSource(previewUrl)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
            } catch (e: Exception) {
                Toast.makeText(this, "Error playing song: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No song selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseSong() {
        mediaPlayer?.pause()
    }

    private fun nextSong() {
        if (songList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList.size
            playSong(songList[currentSongIndex].previewUrl)
        } else {
            Toast.makeText(this, "No more songs.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previousSong() {
        if (songList.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songList.size - 1
            playSong(songList[currentSongIndex].previewUrl)
        } else {
            Toast.makeText(this, "No previous songs.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotification(song: PodcastResult) {
        val pauseIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "PAUSE_ACTION"
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            this, 0, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "PLAY_ACTION"
        }
        val playPendingIntent = PendingIntent.getBroadcast(
            this, 1, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Glide.with(this).asBitmap().load(song.artworkUrl).into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
            override fun onResourceReady(resource: android.graphics.Bitmap, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?) {
                val notification = NotificationCompat.Builder(this@MainActivity, "media_playback_channel")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentTitle(song.trackName)
                    .setContentText(song.artistName)
                    .setLargeIcon(resource)
                    .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
                    .addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent)
                    .setStyle(MediaStyle().setShowActionsInCompactView(0, 1))
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1, notification)
            }

            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_playback_channel",
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
