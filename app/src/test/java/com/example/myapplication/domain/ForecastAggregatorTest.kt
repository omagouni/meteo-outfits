package com.example.myapplication.domain

import com.example.myapplication.data.model.ForecastCity
import com.example.myapplication.data.model.ForecastEntry
import com.example.myapplication.data.model.ForecastResponse
import com.example.myapplication.data.model.Main
import com.example.myapplication.data.model.Weather
import com.example.myapplication.data.model.Wind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ForecastAggregatorTest {

    // 2026-01-05 00:00 UTC
    private val dayStart = 1_767_571_200L

    private fun entry(hourUtc: Int, temp: Double, main: String = "Clear", pop: Double = 0.0, wind: Double = 2.0) =
        ForecastEntry(
            dt = dayStart + hourUtc * 3600L,
            main = Main(temp, temp - 1, temp, temp, 60, 1013),
            weather = listOf(Weather(800, main, main.lowercase(), "01d")),
            wind = Wind(wind, 0),
            pop = pop
        )

    private fun forecast(entries: List<ForecastEntry>, tz: Int = 0) =
        ForecastResponse(entries, ForecastCity("Paris", "FR", tz, 0, 0))

    @Test
    fun `isNight compares UTC timestamps without timezone shift`() {
        val sunrise = 1_000L
        val sunset = 2_000L
        assertTrue(ForecastAggregator.isNight(500, sunrise, sunset))
        assertFalse(ForecastAggregator.isNight(1_500, sunrise, sunset))
        assertTrue(ForecastAggregator.isNight(2_500, sunrise, sunset))
    }

    @Test
    fun `groups entries by local day and ignores night slots`() {
        val entries = listOf(
            entry(3, 2.0),            // nuit : ignoré
            entry(9, 10.0),
            entry(15, 18.0),
            entry(24 + 12, 20.0)      // lendemain
        )
        val days = ForecastAggregator.dailyFrom(forecast(entries), nowUtc = dayStart + 3600, locale = Locale.US)

        assertEquals(2, days.size)
        assertEquals("Today", days[0].label)
        assertEquals("Tomorrow", days[1].label)
        assertEquals(10.0, days[0].tempMin, 0.01)
        assertEquals(18.0, days[0].tempMax, 0.01)
        assertEquals(14.0, days[0].temp, 0.01)
    }

    @Test
    fun `day keeps the most severe condition and highest rain chance`() {
        val entries = listOf(
            entry(9, 12.0, "Clear", pop = 0.1),
            entry(12, 12.0, "Rain", pop = 0.8),
            entry(15, 12.0, "Clouds", pop = 0.3, wind = 10.0)
        )
        val day = ForecastAggregator.dailyFrom(forecast(entries), nowUtc = dayStart).single()

        assertEquals("Rain", day.condition)
        assertEquals(0.8, day.precipitationChance!!, 0.001)
        assertEquals(36.0, day.windKmh, 0.01)
    }

    @Test
    fun `uses city timezone to split days`() {
        // 23h UTC = 01h le lendemain à UTC+2
        val entries = listOf(entry(10, 15.0), entry(23, 5.0), entry(24 + 8, 16.0))
        val days = ForecastAggregator.dailyFrom(forecast(entries, tz = 7200), nowUtc = dayStart)
        assertEquals(2, days.size)
        assertEquals(15.0, days[0].tempMax, 0.01)
    }
}
