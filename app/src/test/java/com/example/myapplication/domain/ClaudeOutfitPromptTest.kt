package com.example.myapplication.domain

import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeOutfitPromptTest {

    private val today = 20_000L

    private val wardrobe = listOf(
        ClothingItem(id = "coat", name = "Navy coat", category = ClothingCategory.OUTERWEAR, warmth = 4, color = "Navy"),
        ClothingItem(id = "rain", name = "Rain jacket", category = ClothingCategory.OUTERWEAR, warmth = 2, waterproof = true),
        ClothingItem(id = "tee", name = "Tee", category = ClothingCategory.TOP, warmth = 1, lastWornEpochDay = today),
        ClothingItem(id = "jeans", name = "Jeans", category = ClothingCategory.BOTTOM, warmth = 3, lastWornEpochDay = today - 2),
        ClothingItem(id = "u1", name = "Umbrella", category = ClothingCategory.ACCESSORY, warmth = 1),
        ClothingItem(id = "u2", name = "Scarf", category = ClothingCategory.ACCESSORY, warmth = 4),
        ClothingItem(id = "u3", name = "Cap", category = ClothingCategory.ACCESSORY, warmth = 1)
    )

    private val day = DayWeather(
        label = "Tomorrow", epochDay = today, temp = 12.0, tempMin = 9.0, tempMax = 15.0, feelsLike = 10.4,
        humidity = 70, windKmh = 18.0, condition = "Rain", description = "light rain",
        precipitationChance = 0.8, isNight = false, isCurrent = false
    )

    @Test
    fun `user message carries weather and wear history`() {
        val msg = ClaudeOutfitPrompt.userMessage(day, wardrobe, today)
        assertTrue(msg.contains("\"feelsLikeC\":10"))
        assertTrue(msg.contains("\"rainChancePercent\":80"))
        assertTrue(msg.contains("\"wornToday\":true"))
        assertTrue(msg.contains("\"lastWornDaysAgo\":2"))
        assertFalse("notes and links are not sent", msg.contains("productUrl"))
    }

    @Test
    fun `parse keeps valid picks and drops unknown ids and duplicates`() {
        val json = """
            {"picks":[
              {"id":"rain","reason":"Waterproof for the rain"},
              {"id":"coat","reason":"second outerwear, should be dropped"},
              {"id":"ghost","reason":"unknown"},
              {"id":"tee","reason":"Already wearing it"},
              {"id":"u1","reason":"80% rain"},{"id":"u2","reason":"cold"},{"id":"u3","reason":"too many"}
            ],
            "missing":[{"category":"SHOES","warmth":9,"waterproof":true},{"category":"TOP","warmth":2,"waterproof":false}],
            "tip":"  Go for navy and yellow.  "}
        """.trimIndent()
        val outfit = ClaudeOutfitPrompt.parse(json, wardrobe)

        assertEquals(listOf("rain", "tee", "u1", "u2"), outfit.picks.map { it.item.id })
        // TOP déjà choisi → retiré des manquants ; chaleur bornée à 5
        assertEquals(listOf(ClothingCategory.SHOES), outfit.missing.map { it.category })
        assertEquals(5, outfit.missing.single().targetWarmth)
        assertEquals("Go for navy and yellow.", outfit.tip)
        assertEquals(PlanSource.AI, outfit.source)
    }

    @Test(expected = IllegalStateException::class)
    fun `parse rejects malformed json`() {
        ClaudeOutfitPrompt.parse("not json", wardrobe)
    }

    @Test(expected = IllegalStateException::class)
    fun `parse rejects an empty outfit`() {
        ClaudeOutfitPrompt.parse("""{"picks":[],"missing":[],"tip":""}""", wardrobe)
    }
}
