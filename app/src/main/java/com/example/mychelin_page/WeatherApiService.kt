package com.example.mychelin_page

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.mychelin_page.WeatherResponse

interface WeatherApiService {
    @GET("weather") // OpenWeather API의 weather 엔드포인트
    fun getWeather(
        @Query("q") cityName: String, // 도시 이름
        @Query("appid") apiKey: String, // API 키
        @Query("units") units: String = "metric" // 온도를 섭씨로 표시
    ): Call<WeatherResponse>
}