package com.example.mychelin_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    // Weather API 관련 변수
    private lateinit var textViewWeatherDescription: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewWeatherDate: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private val apiKey = "65befb3b4c70fcc9281fa50ad5e2cb04"

    // Top rated restaurant 관련 변수
    private lateinit var textViewTopRestaurantName: TextView
    private lateinit var textViewTopRestaurantRating: TextView

    // Most visited restaurant 관련 변수
    private lateinit var textViewMostVisitedRestaurantName: TextView
    private lateinit var textViewMostVisitedRestaurantCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Weather View 연결
        textViewWeatherDescription = view.findViewById(R.id.weather_description)
        textViewTemperature = view.findViewById(R.id.weather_temperature)
        textViewWeatherDate = view.findViewById(R.id.weather_date)
        imageViewWeatherIcon = view.findViewById(R.id.weather_icon)

        // Top Rated Restaurant View 연결
        textViewTopRestaurantName = view.findViewById(R.id.top_restaurant_name)
        textViewTopRestaurantRating = view.findViewById(R.id.top_restaurant_rating)

        // Most Visited Restaurant View 연결
        textViewMostVisitedRestaurantName = view.findViewById(R.id.most_visited_restaurant_name)
        textViewMostVisitedRestaurantCount = view.findViewById(R.id.most_visited_restaurant_count)

        // 날씨 정보 가져오기
        getWeather()

        // Firestore에서 식당 정보 가져오기
        fetchTopRatedRestaurant()
        fetchMostVisitedRestaurant()

        return view
    }

    private fun getWeather() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        val call = service.getWeather("Seoul", apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        textViewWeatherDescription.text = weatherResponse.weather[0].description.capitalize()
                        textViewTemperature.text = "${weatherResponse.main.temp} °C"
                        textViewWeatherDate.text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

                        // 날씨 상태에 따른 아이콘 설정
                        when (weatherResponse.weather[0].description.toLowerCase()) {
                            "clear sky" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_sunny)
                            "few clouds", "scattered clouds", "broken clouds" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_cloudy)
                            "shower rain", "rain" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_rain)
                            "thunderstorm" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_thunderstorm)
                            "snow" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_snow)
                            else -> imageViewWeatherIcon.setImageResource(R.drawable.ic_cloudy)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                textViewWeatherDescription.text = "날씨 정보를 가져오지 못했습니다."
            }
        })
    }

    private fun fetchTopRatedRestaurant() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").get().addOnSuccessListener { userDocuments ->
            var topRestaurantName: String? = null
            var highestRating = Double.MIN_VALUE // null 대신 Double.MIN_VALUE 사용

            for (userDocument in userDocuments) {
                val userId = userDocument.id

                // 각 사용자의 visitData 서브컬렉션 가져오기
                db.collection("users").document(userId).collection("visitData")
                    .get()
                    .addOnSuccessListener { visitDataDocuments ->
                        for (visitDataDocument in visitDataDocuments) {
                            val restaurantName = visitDataDocument.getString("restaurantName")
                            val rating = visitDataDocument.getDouble("rating")

                            if (restaurantName != null && rating != null) {
                                if (rating > highestRating) {
                                    highestRating = rating
                                    topRestaurantName = restaurantName
                                }
                            }
                        }

                        // UI 업데이트
                        textViewTopRestaurantName.text = topRestaurantName ?: "No data"
                        textViewTopRestaurantRating.text = "Rating: ${highestRating}"
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching visitData: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            println("Error fetching users: ${e.message}")
            textViewTopRestaurantName.text = "Failed to load data"
            textViewTopRestaurantRating.text = ""
        }
    }

    private fun fetchMostVisitedRestaurant() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").get().addOnSuccessListener { userDocuments ->
            val visitCounts = mutableMapOf<String, Int>()

            for (userDocument in userDocuments) {
                val userId = userDocument.id

                // 각 사용자의 visitData 서브컬렉션 가져오기
                db.collection("users").document(userId).collection("visitData")
                    .get()
                    .addOnSuccessListener { visitDataDocuments ->
                        for (visitDataDocument in visitDataDocuments) {
                            val restaurantName = visitDataDocument.getString("restaurantName")
                            val visitCount = visitDataDocument.getLong("visitCount")?.toInt() ?: 0

                            if (restaurantName != null) {
                                visitCounts[restaurantName] = visitCounts.getOrDefault(restaurantName, 0) + visitCount
                            }
                        }

                        // 가장 방문 횟수가 많은 식당 찾기
                        val mostVisitedRestaurant = visitCounts.maxByOrNull { it.value }
                        val mostVisitedName = mostVisitedRestaurant?.key ?: "No data"
                        val mostVisitedCount = mostVisitedRestaurant?.value ?: 0

                        // UI 업데이트
                        textViewMostVisitedRestaurantName.text = mostVisitedName
                        textViewMostVisitedRestaurantCount.text = "Visits: $mostVisitedCount"
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching visitData: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            println("Error fetching users: ${e.message}")
            textViewMostVisitedRestaurantName.text = "Failed to load data"
            textViewMostVisitedRestaurantCount.text = ""
        }
    }
}