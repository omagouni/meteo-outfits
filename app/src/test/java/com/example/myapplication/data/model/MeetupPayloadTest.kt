package com.example.myapplication.data.model

import com.example.myapplication.domain.ColorFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeetupPayloadTest {

    private val payload = MeetupPayload(
        from = "Léa", city = "Lyon", day = 20_700, dayLabel = "Sat 21",
        outfit = listOf(
            OutfitPiece("Veste verte", ClothingCategory.OUTERWEAR, ColorFamily.GREEN, "🧥"),
            OutfitPiece("Jean", ClothingCategory.BOTTOM, ColorFamily.DENIM, "👖")
        ),
        feelsLike = 9, condition = "Rain"
    )

    @Test
    fun `round trip through the share link`() {
        val link = payload.toLink()
        assertTrue(link.startsWith(MeetupPayload.LINK_BASE + "#"))
        assertEquals(payload, MeetupPayload.decode(link))
    }

    @Test
    fun `accepts the bare code and surrounding text`() {
        val code = payload.encode()
        assertEquals(payload.city, MeetupPayload.decode("  $code \n")!!.city)
        assertEquals(payload.from, MeetupPayload.decode("Ouvre ce lien : ${payload.toLink()}\n\nPas l'appli ?")!!.from)
    }

    @Test
    fun `rejects garbage`() {
        assertNull(MeetupPayload.decode("hello world"))
        assertNull(MeetupPayload.decode(""))
    }
}
