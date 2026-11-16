package com.example.myapplication.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class LinkPreview(
    val title: String?,
    val imageUrl: String?,
    val brand: String?
)

/**
 * Récupère titre / image / marque d'une page produit à partir de ses balises Open Graph.
 * Beaucoup de boutiques les exposent ; certaines bloquent les requêtes automatisées.
 */
object LinkPreviewFetcher {

    private const val MAX_BYTES = 512L * 1024

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** Ajoute https:// si l'utilisateur a collé un lien sans schéma. Null si invalide. */
    fun normalizeUrl(raw: String): HttpUrl? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if (trimmed.contains("://")) trimmed else "https://$trimmed"
        return withScheme.toHttpUrlOrNull()
    }

    suspend fun fetch(rawUrl: String): Result<LinkPreview> = withContext(Dispatchers.IO) {
        runCatching {
            val url = normalizeUrl(rawUrl) ?: throw IllegalArgumentException("Invalid link")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Mobile")
                .header("Accept", "text/html")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("The site answered ${response.code}")
                val source = response.body?.source() ?: throw IOException("Empty page")
                source.request(MAX_BYTES)
                val html = source.buffer.readUtf8(minOf(source.buffer.size, MAX_BYTES))
                parse(html, response.request.url)
            }
        }
    }

    private val metaTag = Regex("<meta\\s[^>]*>", RegexOption.IGNORE_CASE)
    private val titleTag = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

    private fun attr(tag: String, name: String): String? =
        Regex("""\b$name\s*=\s*("([^"]*)"|'([^']*)')""", RegexOption.IGNORE_CASE)
            .find(tag)
            ?.let { it.groupValues[2].ifEmpty { it.groupValues[3] } }

    fun parse(html: String, baseUrl: HttpUrl): LinkPreview {
        val metas = mutableMapOf<String, String>()
        metaTag.findAll(html).forEach { match ->
            val key = (attr(match.value, "property") ?: attr(match.value, "name"))?.lowercase()
            val content = attr(match.value, "content")
            if (key != null && !content.isNullOrBlank() && key !in metas) {
                metas[key] = decodeEntities(content.trim())
            }
        }

        val title = metas["og:title"] ?: metas["twitter:title"]
            ?: titleTag.find(html)?.groupValues?.get(1)?.let { decodeEntities(it.trim()) }
        val image = (metas["og:image:secure_url"] ?: metas["og:image"] ?: metas["twitter:image"])
            ?.let { baseUrl.resolve(it)?.toString() }
        val brand = metas["product:brand"] ?: metas["og:brand"] ?: metas["og:site_name"]

        return LinkPreview(title?.takeIf { it.isNotBlank() }, image, brand)
    }

    private fun decodeEntities(text: String): String = text
        .replace(Regex("&#(\\d+);")) { it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: it.value }
        .replace("&quot;", "\"")
        .replace("&#x27;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
}
