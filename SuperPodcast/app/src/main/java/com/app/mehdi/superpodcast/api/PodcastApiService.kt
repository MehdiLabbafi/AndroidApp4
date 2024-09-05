package com.app.mehdi.superpodcast

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastApiService {
    @GET("search")
    fun searchByArtist(
        @Query("term") artistName: String,
        @Query("media") media: String = "music"  // media type is set to music
    ): Call<PodcastResponse>
}