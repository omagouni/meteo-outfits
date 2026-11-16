package com.example.myapplication.data.remote

import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
        // Clé définie dans local.properties : WEATHER_API_KEY=ta_cle
        // (clé gratuite sur https://openweathermap.org/appid)
        val API_KEY: String = BuildConfig.WEATHER_API_KEY
    }
}
