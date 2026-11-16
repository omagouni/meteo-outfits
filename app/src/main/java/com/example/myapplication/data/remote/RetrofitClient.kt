package com.example.myapplication.data.remote

import com.example.myapplication.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /** Clé définie dans local.properties : WEATHER_API_KEY=ta_cle */
    val apiKey: String = BuildConfig.WEATHER_API_KEY

    // Logs uniquement en debug. Le logger est placé AVANT l'ajout de la clé,
    // donc la clé n'apparaît jamais dans logcat.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request()
        // Clé API + langue du téléphone (descriptions météo traduites par OpenWeatherMap)
        val url = request.url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .addQueryParameter("lang", java.util.Locale.getDefault().language.ifBlank { "en" })
            .build()
        chain.proceed(request.newBuilder().url(url).build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
