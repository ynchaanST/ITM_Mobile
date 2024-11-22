package com.example.mychelin_page

data class WeatherResponse(
    val weather: List<Weather>, // weather 배열
    val main: Main, // 온도 정보
    val name: String // 도시 이름
)

data class Weather(
    val description: String, // 날씨 설명
    val icon: String // 날씨 아이콘 코드
)

data class Main(
    val temp: Double, // 현재 온도
    val feels_like: Double, // 체감 온도
    val temp_min: Double, // 최저 온도
    val temp_max: Double, // 최고 온도
    val pressure: Int, // 기압
    val humidity: Int // 습도
)