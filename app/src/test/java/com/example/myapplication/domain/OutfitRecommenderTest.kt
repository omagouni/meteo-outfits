package com.example.myapplication.domain

import com.example.myapplication.R
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.ui.UiText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitRecommenderTest {

    private fun day(
        temp: Double = 15.0,
        feelsLike: Double = temp,
        min: Double = temp,
        max: Double = temp,
        condition: String = "Clouds",
        pop: Double? = null,
        wind: Double = 5.0,
        isCurrent: Boolean = false
    ) = DayWeather(
        label = "Today", epochDay = 0, temp = temp, tempMin = min, tempMax = max,
        feelsLike = feelsLike, humidity = 50, windKmh = wind, condition = condition,
        description = condition, precipitationChance = pop, isNight = false, isCurrent = isCurrent
    )

    private fun names(d: DayWeather): List<Int> =
        OutfitRecommender.getOutfit(d).let { it.mainOutfit + it.accessories }
            .map { (it.name as UiText.Res).id }

    @Test
    fun `dresses for feels-like temperature`() {
        assertTrue(R.string.oi_heavy_coat in names(day(temp = 6.0, feelsLike = 1.0)))
    }

    @Test
    fun `high rain probability adds umbrella even if condition is cloudy`() {
        assertTrue(R.string.oi_umbrella in names(day(condition = "Clouds", pop = 0.7)))
        assertFalse(R.string.oi_umbrella in names(day(condition = "Clouds", pop = 0.2)))
    }

    @Test
    fun `large temperature swing suggests removable layer for forecast days only`() {
        assertTrue(R.string.oi_removable_layer in names(day(min = 8.0, max = 20.0)))
        assertFalse(R.string.oi_removable_layer in names(day(min = 8.0, max = 20.0, isCurrent = true)))
    }

    @Test
    fun `strong wind adds windbreaker`() {
        assertTrue(R.string.oi_windbreaker in names(day(wind = 40.0)))
    }
}
