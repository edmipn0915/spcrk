package com.spcrk.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    private val engine = SearchEngine()

    @Test
    fun `parseDuckDuckGoResults extracts titles and URLs from HTML`() {
        val html = """
            <html>
            <div class="result">
                <a class="result__a" href="https://example.com/1">First Result</a>
                <a class="result__snippet">Some snippet text here</a>
            </div>
            <div class="result">
                <a class="result__a" href="https://example.com/2">Second Result</a>
                <a class="result__snippet">Another snippet</a>
            </div>
            </html>
        """.trimIndent()

        val results = engine.parseDuckDuckGoResults(html, 5)

        assertEquals(2, results.size)
        assertEquals("First Result", results[0].title)
        assertEquals("Some snippet text here", results[0].snippet)
        assertEquals("https://example.com/1", results[0].url)
        assertEquals("Second Result", results[1].title)
    }

    @Test
    fun `parseDuckDuckGoResults handles empty HTML`() {
        val results = engine.parseDuckDuckGoResults("<html></html>", 5)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `parseDuckDuckGoResults respects maxResults`() {
        val html = """
            <html>
            <div class="result"><a class="result__a" href="/a">A</a></div>
            <div class="result"><a class="result__a" href="/b">B</a></div>
            <div class="result"><a class="result__a" href="/c">C</a></div>
            </html>
        """.trimIndent()

        val results = engine.parseDuckDuckGoResults(html, 2)
        assertEquals(2, results.size)
    }

    @Test
    fun `parseDuckDuckGoResults filters duckduckgo internal links`() {
        val html = """
            <html>
            <div class="result">
                <a class="result__a" href="https://duckduckgo.com/y.js">Spam</a>
            </div>
            </html>
        """.trimIndent()

        val results = engine.parseDuckDuckGoResults(html, 5)
        assertTrue(results.isEmpty())
    }
}
