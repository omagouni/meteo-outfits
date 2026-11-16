package com.example.myapplication.domain

import com.example.myapplication.data.model.DayWeather
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

    private fun names(d: DayWeather) =
        OutfitRecommender.getOutfit(d).let { it.mainOutfit + it.accessories }.map { it.name }

    @Test
    fun `dresses for feels-like temperature`() {
        val outfit = names(day(temp = 6.0, feelsLike = 1.0))
        assertTrue("Heavy Coat" in outfit)
    }

    @Test
    fun `high rain probability adds umbrella even if condition is cloudy`() {
        assertTrue("Umbrella" in names(day(condition = "Clouds", pop = 0.7)))
        assertFalse("Umbrella" in names(day(condition = "Clouds", pop = 0.2)))
    }

    @Test
    fun `large temperature swing suggests removable layer for forecast days only`() {
        assertTrue("Removable Layer" in names(day(min = 8.0, max = 20.0)))
        assertFalse("Removable Layer" in names(day(min = 8.0, max = 20.0, isCurrent = true)))
    }

    @Test
    fun `strong wind adds windbreaker`() {
        assertTrue("Windbreaker" in names(day(wind = 40.0)))
    }
}
