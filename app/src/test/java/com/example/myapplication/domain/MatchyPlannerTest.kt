package com.example.myapplication.domain

import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingCategory.BOTTOM
import com.example.myapplication.data.model.ClothingCategory.OUTERWEAR
import com.example.myapplication.data.model.ClothingCategory.SHOES
import com.example.myapplication.data.model.ClothingCategory.TOP
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchyPlannerTest {

    private val today = 20_000L

    private fun day(feelsLike: Double, condition: String = "Clouds") = DayWeather(
        label = "Today", epochDay = today, temp = feelsLike, tempMin = feelsLike, tempMax = feelsLike,
        feelsLike = feelsLike, humidity = 50, windKmh = 5.0, condition = condition,
        description = condition, precipitationChance = null, isNight = false, isCurrent = false
    )

    private fun item(id: String, cat: ClothingCategory, warmth: Int, color: String) =
        ClothingItem(id = id, name = id, category = cat, warmth = warmth, color = color)

    @Test
    fun `color names in French and English map to families`() {
        assertEquals(ColorFamily.NAVY, ColorFamily.fromText("Bleu marine"))
        assertEquals(ColorFamily.BLUE, ColorFamily.fromText("blue"))
        assertEquals(ColorFamily.PINK, ColorFamily.fromText("rose poudré"))
        assertEquals(ColorFamily.BEIGE, ColorFamily.fromText("Camel"))
        assertEquals(ColorFamily.UNKNOWN, ColorFamily.fromText("paisley"))
        assertEquals(ColorFamily.UNKNOWN, ColorFamily.fromText(""))
    }

    @Test
    fun `pair score rewards a shared accent and penalises clashes`() {
        val shared = OutfitHarmony.pairScore(listOf(ColorFamily.GREEN, ColorFamily.BLACK), listOf(ColorFamily.GREEN, ColorFamily.WHITE))
        val clash = OutfitHarmony.pairScore(listOf(ColorFamily.RED), listOf(ColorFamily.GREEN))
        val neutral = OutfitHarmony.pairScore(listOf(ColorFamily.BLACK), listOf(ColorFamily.WHITE))
        assertTrue(shared > neutral)
        assertTrue(clash < 0)
    }

    @Test
    fun `picks the top that matches the partner accent color`() {
        val wardrobe = listOf(
            item("coat", OUTERWEAR, 3, "noir"),
            item("tee-green", TOP, 2, "vert"),
            item("tee-red", TOP, 2, "rouge"),
            item("jeans", BOTTOM, 3, "denim"),
            item("sneakers", SHOES, 2, "blanc")
        )
        val result = MatchyPlanner.plan(day(10.0), wardrobe, listOf(ColorFamily.GREEN, ColorFamily.BEIGE), "Léa", today)
        val ids = result.outfit.picks.map { it.item.id }
        assertTrue("tee-green" in ids)
        assertEquals(ColorFamily.GREEN, result.linkColor)
        assertTrue(result.pairScore >= 3.0)
    }

    @Test
    fun `weather still wins over color`() {
        val wardrobe = listOf(
            item("tee-green", TOP, 1, "vert"),   // assorti mais bien trop léger pour 2°
            item("sweater-grey", TOP, 4, "gris"),
            item("jeans", BOTTOM, 3, "denim"),
            item("boots", SHOES, 4, "noir")
        )
        val result = MatchyPlanner.plan(day(2.0), wardrobe, listOf(ColorFamily.GREEN), "Léa", today)
        val ids = result.outfit.picks.map { it.item.id }
        assertTrue("sweater-grey" in ids)
    }

    @Test
    fun `virtual wardrobe from chosen colors still gives an outfit`() {
        val virtual = MatchyPlanner.virtualWardrobe(
            mapOf(TOP to ColorFamily.PINK, BOTTOM to ColorFamily.BLACK, SHOES to ColorFamily.WHITE),
            feelsLike = 22.0
        )
        val result = MatchyPlanner.plan(day(22.0, "Clear"), virtual, listOf(ColorFamily.PINK), "Sam", today)
        assertEquals(3, result.outfit.picks.size)
        assertEquals(ColorFamily.PINK, result.linkColor)
    }
}
