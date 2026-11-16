package com.example.myapplication.data.remote

import androidx.annotation.StringRes
import com.example.myapplication.R
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.domain.ForecastAggregator
import com.example.myapplication.ui.UiText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

/** Météo d'une ville : maintenant + jours de prévision, prête pour la recommandation. */
object WeatherRepository {

    data class CityWeather(val cityLabel: String, val days: List<DayWeather>)

    /** Charge météo actuelle + prévisions en parallèle. Les prévisions sont optionnelles. */
    suspend fun load(cityName: String): CityWeather = coroutineScope {
        val api = RetrofitClient.weatherApi
        val forecastDeferred = async { runCatching { api.getForecast(cityName) } }
        val current = api.getWeather(cityName)
        val forecastDays = forecastDeferred.await()
            .getOrNull()
            ?.let { ForecastAggregator.dailyFrom(it) }
            .orEmpty()
        CityWeather("${current.cityName}, ${current.sys.country}", listOf(ForecastAggregator.fromCurrent(current)) + forecastDays)
    }

    fun errorText(e: Exception): UiText = when (e) {
        is HttpException -> when (e.code()) {
            404 -> UiText.res(R.string.err_city_not_found)
            401 -> UiText.res(R.string.err_invalid_key)
            429 -> UiText.res(R.string.err_too_many)
            else -> UiText.res(R.string.err_service, e.code())
        }
        is UnknownHostException -> UiText.res(R.string.err_no_internet)
        is IOException -> UiText.res(R.string.err_network)
        else -> UiText.Res(R.string.err_generic, listOf(e.localizedMessage ?: UiText.res(R.string.err_unknown)))
    }

    @StringRes
    fun missingKeyRes(): Int = R.string.err_missing_key
}
