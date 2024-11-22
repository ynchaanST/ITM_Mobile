package com.example.mychelin_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import com.example.mychelin_page.WeatherResponse

class HomeFragment : Fragment() {

    private lateinit var textViewWeatherDescription: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewWeatherDate: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private val apiKey = "65befb3b4c70fcc9281fa50ad5e2cb04"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // View 연결
        textViewWeatherDescription = view.findViewById(R.id.weather_description)
        textViewTemperature = view.findViewById(R.id.weather_temperature)
        textViewWeatherDate = view.findViewById(R.id.weather_date)
        imageViewWeatherIcon = view.findViewById(R.id.weather_icon)

        // 날씨 정보 가져오기
        getWeather()

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
}