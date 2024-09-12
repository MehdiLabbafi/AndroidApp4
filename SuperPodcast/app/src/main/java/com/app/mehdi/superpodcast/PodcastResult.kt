package com.app.mehdi.superpodcast

import com.google.gson.annotations.SerializedName

data class PodcastResponse(
    @SerializedName("results")
    val results: List<PodcastResult>
)

data class PodcastResult(
    @SerializedName("artistName")
    val artistName: String,
    @SerializedName("trackName")
    val trackName: String,
    @SerializedName("collectionName")
    val collectionName: String,
    @SerializedName("artworkUrl100")
    val artworkUrl: String,
    @SerializedName("previewUrl")
    val previewUrl: String    // Link to play the song
)
