package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ForecastResponse
import com.example.myapplication.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** La clé API (`appid`) est ajoutée par un intercepteur dans [RetrofitClient]. */
interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }
}
