package com.example.mychelin_page

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverApiService {
    @GET("v1/search/local.json")
    fun searchPlaces(
        @Query("query") query: String,
        @Query("display") display: Int = 20,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random",
        @Query("coordinate") coordinate: String,
        @Query("radius") radius: Int
    ): Call<NaverSearchResponse>
}

