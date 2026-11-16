package com.example.myapplication.domain

import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.model.ForecastEntry
import com.example.myapplication.data.model.ForecastResponse
import com.example.myapplication.data.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Transforme les réponses brutes de l'API en [DayWeather] exploitables par la recommandation. */
object ForecastAggregator {

    private const val SECONDS_PER_DAY = 86_400L

    /** Heures locales considérées comme "la journée" (quand on est dehors). */
    private val DAYTIME_HOURS = 7..21

    /** sunrise, sunset et now sont tous des timestamps UTC : pas de décalage horaire à appliquer. */
    fun isNight(nowUtc: Long, sunriseUtc: Long, sunsetUtc: Long): Boolean =
        nowUtc < sunriseUtc || nowUtc > sunsetUtc

    fun fromCurrent(response: WeatherResponse): DayWeather {
        val weather = response.weather.firstOrNull()
        return DayWeather(
            label = "Now",
            epochDay = null,
            temp = response.main.temp,
            tempMin = response.main.tempMin,
            tempMax = response.main.tempMax,
            feelsLike = response.main.feelsLike,
            humidity = response.main.humidity,
            windKmh = response.wind.speed * 3.6,
            condition = weather?.main ?: "Clear",
            description = weather?.description.orEmpty(),
            precipitationChance = null,
            isNight = isNight(response.dt, response.sys.sunrise, response.sys.sunset),
            isCurrent = true
        )
    }

    /**
     * Regroupe les prévisions par jour local et résume chaque journée sur ses heures de jour.
     * Les jours sans aucun créneau de journée restant (ex. aujourd'hui à 23 h) sont ignorés.
     */
    fun dailyFrom(
        forecast: ForecastResponse,
        nowUtc: Long = System.currentTimeMillis() / 1000,
        maxDays: Int = 5,
        locale: Locale = Locale.getDefault()
    ): List<DayWeather> {
        val offset = forecast.city.timezone.toLong()
        val today = Math.floorDiv(nowUtc + offset, SECONDS_PER_DAY)

        return forecast.list
            .groupBy { Math.floorDiv(it.dt + offset, SECONDS_PER_DAY) }
            .toSortedMap()
            .mapNotNull { (day, entries) ->
                val daytime = entries.filter { localHour(it.dt, offset) in DAYTIME_HOURS }
                if (daytime.isEmpty()) null else summarize(day, daytime, today, locale)
            }
            .take(maxDays)
    }

    private fun summarize(day: Long, entries: List<ForecastEntry>, today: Long, locale: Locale): DayWeather {
        val worst = entries.maxBy { severity(it.weather.firstOrNull()?.main.orEmpty()) }
        val worstWeather = worst.weather.firstOrNull()
        return DayWeather(
            label = labelFor(day, today, locale),
            epochDay = day,
            temp = entries.map { it.main.temp }.average(),
            tempMin = entries.minOf { it.main.tempMin },
            tempMax = entries.maxOf { it.main.tempMax },
            feelsLike = entries.map { it.main.feelsLike }.average(),
            humidity = entries.map { it.main.humidity }.average().toInt(),
            windKmh = entries.maxOf { it.wind.speed } * 3.6,
            condition = worstWeather?.main ?: "Clear",
            description = worstWeather?.description.orEmpty(),
            precipitationChance = entries.maxOf { it.pop },
            isNight = false,
            isCurrent = false
        )
    }

    /** Plus le score est élevé, plus la condition impacte la tenue. */
    fun severity(condition: String): Int = when (condition.lowercase()) {
        "thunderstorm" -> 6
        "snow" -> 5
        "rain" -> 4
        "drizzle" -> 3
        "mist", "fog", "haze", "smoke", "dust", "sand" -> 2
        "clouds" -> 1
        else -> 0
    }

    private fun localHour(dtUtc: Long, offset: Long): Long =
        Math.floorMod(dtUtc + offset, SECONDS_PER_DAY) / 3600

    private fun labelFor(day: Long, today: Long, locale: Locale): String = when (day) {
        today -> "Today"
        today + 1 -> "Tomorrow"
        else -> SimpleDateFormat("EEE d", locale)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(day * SECONDS_PER_DAY * 1000))
            .replaceFirstChar { it.uppercase() }
    }
}
