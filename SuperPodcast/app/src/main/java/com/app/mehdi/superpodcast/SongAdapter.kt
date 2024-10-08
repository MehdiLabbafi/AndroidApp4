package com.app.mehdi.superpodcast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SongAdapter(
    private val songs: List<PodcastResult>,
    private val onSongClick: (PodcastResult) -> Unit // Add click as parameter
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistName: TextView = itemView.findViewById(R.id.artistNameTextView)
        val trackName: TextView = itemView.findViewById(R.id.trackNameTextView)
        val artwork: ImageView = itemView.findViewById(R.id.artworkImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.artistName.text = song.artistName
        holder.trackName.text = song.trackName

        // Load the cover image
        Glide.with(holder.itemView.context)
            .load(song.artworkUrl)
            .into(holder.artwork)

        // Set item click
        holder.itemView.setOnClickListener {
            onSongClick(song) // This function is called when the item is clicked
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}
