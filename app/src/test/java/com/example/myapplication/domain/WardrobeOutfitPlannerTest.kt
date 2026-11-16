package com.example.myapplication.domain

import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingCategory.ACCESSORY
import com.example.myapplication.data.model.ClothingCategory.BOTTOM
import com.example.myapplication.data.model.ClothingCategory.OUTERWEAR
import com.example.myapplication.data.model.ClothingCategory.SHOES
import com.example.myapplication.data.model.ClothingCategory.TOP
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeOutfitPlannerTest {

    private val today = 20_000L

    private fun day(feelsLike: Double, condition: String = "Clouds", pop: Double? = null, wind: Double = 5.0) =
        DayWeather(
            label = "Today", epochDay = today, temp = feelsLike, tempMin = feelsLike, tempMax = feelsLike,
            feelsLike = feelsLike, humidity = 50, windKmh = wind, condition = condition,
            description = condition, precipitationChance = pop, isNight = false, isCurrent = false
        )

    private fun item(
        id: String, category: ClothingCategory, warmth: Int,
        waterproof: Boolean = false, lastWorn: Long? = null
    ) = ClothingItem(id = id, name = id, category = category, warmth = warmth, waterproof = waterproof, lastWornEpochDay = lastWorn)

    private fun WardrobeOutfit.ids() = picks.map { it.item.id }

    private val wardrobe = listOf(
        item("parka", OUTERWEAR, 5),
        item("raincoat", OUTERWEAR, 2, waterproof = true),
        item("tee", TOP, 1),
        item("sweater", TOP, 4),
        item("shorts", BOTTOM, 1),
        item("jeans", BOTTOM, 3),
        item("sandals", SHOES, 1),
        item("boots", SHOES, 4, waterproof = true),
        item("scarf", ACCESSORY, 4),
        item("umbrella", ACCESSORY, 1, waterproof = true)
    )

    @Test
    fun `hot day picks light clothes and no outerwear`() {
        val outfit = RuleBasedOutfitPlanner.plan(day(28.0, "Clear"), wardrobe, today)
        assertEquals(listOf("tee", "shorts", "sandals"), outfit.ids())
    }

    @Test
    fun `freezing day picks warm layers and accessories`() {
        val ids = RuleBasedOutfitPlanner.plan(day(-3.0), wardrobe, today).ids()
        assertTrue("parka" in ids)
        assertTrue("sweater" in ids)
        assertTrue("boots" in ids)
        assertTrue("scarf" in ids)
    }

    @Test
    fun `rainy mild day prefers waterproof pieces and umbrella`() {
        val ids = RuleBasedOutfitPlanner.plan(day(17.0, "Clouds", pop = 0.8), wardrobe, today).ids()
        assertTrue("raincoat" in ids)
        assertTrue("umbrella" in ids)
    }

    @Test
    fun `avoids what was worn yesterday when an alternative exists`() {
        val tops = listOf(item("tee-a", TOP, 1, lastWorn = today - 1), item("tee-b", TOP, 1))
        val ids = RuleBasedOutfitPlanner.plan(day(28.0, "Clear"), tops, today).ids()
        assertEquals(listOf("tee-b"), ids)
    }

    @Test
    fun `keeps what is already worn today`() {
        val tops = listOf(item("tee-a", TOP, 1), item("tee-b", TOP, 1, lastWorn = today))
        val ids = RuleBasedOutfitPlanner.plan(day(28.0, "Clear"), tops, today).ids()
        assertEquals(listOf("tee-b"), ids)
    }

    @Test
    fun `reports missing slots`() {
        val outfit = RuleBasedOutfitPlanner.plan(day(-3.0, "Rain"), listOf(item("tee", TOP, 1)), today)
        val missing = outfit.missing.map { it.category }
        assertTrue(OUTERWEAR in missing)
        assertTrue(outfit.missing.first { it.category == OUTERWEAR }.needsWaterproof)
        // un t-shirt (1) est trop léger pour un haut cible de 4 → manquant aussi
        assertTrue(TOP in missing)
    }
}
