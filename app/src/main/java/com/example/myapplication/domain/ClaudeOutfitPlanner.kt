package com.example.myapplication.domain

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.models.beta.messages.BetaJsonOutputFormat
import com.anthropic.models.beta.messages.BetaOutputConfig
import com.anthropic.models.beta.messages.BetaStopReason
import com.anthropic.models.beta.messages.MessageCreateParams
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

/**
 * Styliste IA : envoie la météo du jour + la garde-robe à Claude et reçoit une tenue en JSON
 * (sortie structurée). Lève une exception en cas d'échec ; l'appelant retombe sur les règles.
 */
class ClaudeOutfitPlanner(private val apiKey: String = BuildConfig.ANTHROPIC_API_KEY) : OutfitPlanner {

    val isConfigured: Boolean get() = apiKey.isNotBlank()

    private val client: AnthropicClient by lazy {
        AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(60))
            .maxRetries(1)
            .build()
    }

    private val outputConfig by lazy {
        val schema = BetaJsonOutputFormat.Schema.builder().apply {
            ClaudeOutfitPrompt.RESPONSE_SCHEMA.forEach { (key, value) ->
                putAdditionalProperty(key, JsonValue.from(value))
            }
        }.build()
        BetaOutputConfig.builder()
            .format(BetaJsonOutputFormat.builder().schema(schema).build())
            // Choix simple et fréquent sur mobile : effort bas = réponse rapide et peu coûteuse.
            .effort(BetaOutputConfig.Effort.LOW)
            .build()
    }

    override suspend fun plan(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): WardrobeOutfit {
        check(isConfigured) { "No Claude API key configured" }
        val params = MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(16000L)
            .system(ClaudeOutfitPrompt.SYSTEM_PROMPT)
            .outputConfig(outputConfig)
            // Si un filtre de sécurité refuse la requête, l'API la rejoue sur un autre modèle.
            .addBeta(SERVER_SIDE_FALLBACK_BETA)
            .fallbacksDefault()
            .addUserMessage(ClaudeOutfitPrompt.userMessage(day, wardrobe, referenceDay))
            .build()

        // Le SDK est bloquant : on sort du thread principal.
        val message = withContext(Dispatchers.IO) { client.beta().messages().create(params) }

        when (message.stopReason().orElse(null)) {
            BetaStopReason.REFUSAL -> throw IllegalStateException("The AI stylist declined this request")
            BetaStopReason.MAX_TOKENS -> throw IllegalStateException("The AI answer was cut off")
            else -> Unit
        }
        val json = message.content().mapNotNull { it.text().orElse(null)?.text() }.joinToString("")
        return ClaudeOutfitPrompt.parse(json, wardrobe)
    }

    companion object {
        const val MODEL = "claude-opus-5-5"
        private const val SERVER_SIDE_FALLBACK_BETA = "server-side-fallback-2026-07-01"
    }
}
