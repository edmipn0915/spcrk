package com.spcrk.app.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatStreamProcessorTest {

    private val processor = ChatStreamProcessor(AIManager())

    @Test
    fun `parseAgentResponse extracts normal content from plain text`() {
        val response = "Just plain text"
        val result = processor.parseAgentResponse(response)
        assertEquals("Just plain text", result.normalContent)
    }

    @Test
    fun `parseAgentResponse returns empty thinkContent when no tags`() {
        val response = "Hello world"
        val result = processor.parseAgentResponse(response)
        assertEquals("", result.thinkContent)
        assertEquals("Hello world", result.normalContent)
    }

    @Test
    fun `StreamResult carries text and tool calls`() {
        val result = StreamResult(
            text = "hello",
            toolCalls = listOf(Triple("id1", "tool", "{}"))
        )
        assertEquals("hello", result.text)
        assertEquals(1, result.toolCalls.size)
    }

    @Test
    fun `ParsedAgentResponse holds think and normal content`() {
        val parsed = ParsedAgentResponse(
            thinkContent = "thinking...",
            normalContent = "answer"
        )
        assertEquals("thinking...", parsed.thinkContent)
        assertEquals("answer", parsed.normalContent)
    }

    @Test
    fun `parseAgentResponse extracts content inside thinking tags`() {
        val response = "<thinking>Let me analyze this step by step.</thinking>The answer is 42."
        val result = processor.parseAgentResponse(response)
        assertEquals("Let me analyze this step by step.", result.thinkContent)
        assertEquals("The answer is 42.", result.normalContent)
    }

    @Test
    fun `parseAgentResponse strips all thinking blocks from normal content`() {
        val response = "First<thinking>hidden</thinking>Second<thinking>also hidden</thinking>Third"
        val result = processor.parseAgentResponse(response)
        assertEquals("hidden\nalso hidden", result.thinkContent)
        assertEquals("FirstSecondThird", result.normalContent)
    }

    @Test
    fun `parseAgentResponse trims surrounding whitespace`() {
        val response = "  <thinking>  spaced thinking  </thinking>  spaced answer  "
        val result = processor.parseAgentResponse(response)
        assertEquals("spaced thinking", result.thinkContent)
        assertEquals("spaced answer", result.normalContent)
    }
}
