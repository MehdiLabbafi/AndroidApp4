package com.app.mehdi.superpodcast

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mehdi.superpodcast.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener {
            val searchQuery = binding.searchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchPodcasts(searchQuery)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
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
                        displaySongs(results)
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
        val adapter = SongAdapter(songs)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
}
