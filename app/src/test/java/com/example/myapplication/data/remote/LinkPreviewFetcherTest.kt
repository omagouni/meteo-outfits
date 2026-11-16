package com.example.myapplication.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LinkPreviewFetcherTest {

    private val base = "https://shop.example.com/p/123".toHttpUrl()

    @Test
    fun `reads open graph tags and resolves relative image`() {
        val html = """
            <html><head>
            <meta property="og:title" content="Wool Coat &amp; Belt" />
            <meta content='/img/coat.jpg' property='og:image'>
            <meta property="og:site_name" content="ExampleShop">
            <title>Ignored</title>
            </head></html>
        """.trimIndent()
        val preview = LinkPreviewFetcher.parse(html, base)
        assertEquals("Wool Coat & Belt", preview.title)
        assertEquals("https://shop.example.com/img/coat.jpg", preview.imageUrl)
        assertEquals("ExampleShop", preview.brand)
    }

    @Test
    fun `falls back to title tag`() {
        val preview = LinkPreviewFetcher.parse("<title>\n Rain Jacket </title>", base)
        assertEquals("Rain Jacket", preview.title)
        assertNull(preview.imageUrl)
    }

    @Test
    fun `product brand wins over site name`() {
        val html = """<meta property="og:site_name" content="Mall"><meta property="product:brand" content="Acme">"""
        assertEquals("Acme", LinkPreviewFetcher.parse(html, base).brand)
    }

    @Test
    fun `normalizes links without scheme`() {
        assertEquals("https://shop.com/item", LinkPreviewFetcher.normalizeUrl(" shop.com/item ").toString())
        assertNull(LinkPreviewFetcher.normalizeUrl(""))
    }
}
