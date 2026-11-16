package com.example.myapplication.data.model

import com.example.myapplication.domain.ColorFamily
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Une pièce de tenue telle qu'elle voyage dans une invitation : juste de quoi coordonner. */
data class OutfitPiece(
    val name: String = "",
    val category: ClothingCategory = ClothingCategory.TOP,
    val color: ColorFamily = ColorFamily.UNKNOWN,
    val emoji: String = ""
)

/**
 * Ce qui est envoyé dans le lien d'invitation. Volontairement minimal :
 * pas de liens produits, pas de notes, pas de garde-robe complète.
 */
@OptIn(ExperimentalEncodingApi::class)
data class MeetupPayload(
    val v: Int = 1,
    val id: String = UUID.randomUUID().toString(),
    val from: String = "",
    val city: String = "",
    /** Jour local du rendez-vous (jours depuis l'epoch). */
    val day: Long = 0,
    val dayLabel: String = "",
    val outfit: List<OutfitPiece> = emptyList(),
    /** Météo vue par l'expéditeur, à titre indicatif (le destinataire la recalcule). */
    val feelsLike: Int? = null,
    val condition: String = ""
) {
    val colors: List<ColorFamily> get() = outfit.map { it.color }

    fun encode(): String = codec.encode(gson.toJson(this).toByteArray(Charsets.UTF_8))

    /** Lien complet à partager. Le payload est dans le fragment : jamais envoyé au serveur. */
    fun toLink(): String = "$LINK_BASE#${encode()}"

    companion object {
        private val gson = Gson()
        private val codec = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

        /**
         * Page d'atterrissage (GitHub Pages du dépôt). Si l'appli est installée, Android l'ouvre
         * directement grâce au filtre d'intent ; sinon la page propose d'installer l'appli.
         */
        const val LINK_HOST = "omagouni.github.io"
        const val LINK_PATH = "/meteo-outfits/m/"
        const val LINK_BASE = "https://$LINK_HOST$LINK_PATH"
        const val STORE_URL = "https://play.google.com/store/apps/details?id=com.meteooutfits.app"

        /** Accepte un lien complet, ou juste le code collé. Null si ce n'est pas une invitation. */
        fun decode(text: String): MeetupPayload? {
            val raw = text.trim()
            if (raw.isEmpty()) return null
            val code = raw.substringAfterLast('#').trim().takeWhile { !it.isWhitespace() }
            return try {
                val json = String(codec.decode(code), Charsets.UTF_8)
                gson.fromJson(json, MeetupPayload::class.java)?.takeIf { it.v >= 1 && it.city.isNotBlank() }
            } catch (_: IllegalArgumentException) {
                null
            } catch (_: JsonSyntaxException) {
                null
            }
        }
    }
}

enum class MeetupDirection { SENT, RECEIVED }

/** Un rendez-vous enregistré sur le téléphone. */
data class Meetup(
    val id: String = UUID.randomUUID().toString(),
    val direction: MeetupDirection = MeetupDirection.SENT,
    val friendName: String = "",
    val city: String = "",
    val day: Long = 0,
    val dayLabel: String = "",
    /** Ma tenue pour ce rendez-vous. */
    val myOutfit: List<OutfitPiece> = emptyList(),
    /** La tenue de l'ami, connue si c'est une invitation reçue ou s'il a renvoyé la sienne. */
    val friendOutfit: List<OutfitPiece> = emptyList(),
    val feelsLike: Int? = null,
    val condition: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
