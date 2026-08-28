package com.spcrk.app.ai

import com.spcrk.app.ai.api.McpConnectionState
import com.spcrk.app.data.McpServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * McpManager 单元测试（纯 JVM，无需 Android 环境）。
 */
class McpManagerTest {

    private val manager = McpManager()

    @Test
    fun `addServer stores and returns server`() {
        val server = McpServer(id = 1L, name = "test", connectionType = "stdio", command = "echo", args = "")
        manager.addServer(server)
        assertEquals(server, manager.getServer("1"))
    }

    @Test
    fun `removeServer removes from store`() {
        val server = McpServer(id = 2L, name = "del", connectionType = "stdio", command = "echo", args = "")
        manager.addServer(server)
        manager.removeServer("2")
        assertNull(manager.getServer("2"))
    }

    @Test
    fun `getServers returns all servers`() {
        manager.addServer(McpServer(id = 1L, name = "a", connectionType = "stdio", command = "cmd", args = ""))
        manager.addServer(McpServer(id = 2L, name = "b", connectionType = "sse", command = "", args = "", url = "http://x"))
        assertEquals(2, manager.getServers().size)
    }

    @Test
    fun `connectionState defaults to DISCONNECTED`() {
        assertEquals(McpConnectionState.DISCONNECTED, manager.getConnectionState("nonexistent"))
    }

    @Test
    fun `getToolsForServer returns empty when no tools`() {
        assertTrue(manager.getToolsForServer("x").isEmpty())
    }

    @Test
    fun `parseJsonConfig creates server from valid JSON`() {
        val json = """{"name":"my-server","type":"stdio","command":"/bin/echo","args":"hello"}"""
        val server = manager.parseJsonConfig(json, "json")
        assertNotNull(server)
        assertEquals("my-server", server?.name)
        assertEquals("stdio", server?.connectionType)
        assertEquals("/bin/echo", server?.command)
        assertEquals("hello", server?.args)
        assertEquals("json", server?.source)
    }

    @Test
    fun `parseJsonConfig returns null for invalid JSON`() {
        assertNull(manager.parseJsonConfig("{invalid", "json"))
    }

    @Test
    fun `parseJsonConfig returns null when name is empty`() {
        assertNull(manager.parseJsonConfig("""{"type":"stdio","command":"x"}""", "json"))
    }

    @Test
    fun `disconnectServer sets state to DISCONNECTED`() {
        val server = McpServer(id = 10L, name = "s", connectionType = "stdio", command = "c", args = "")
        manager.addServer(server)
        manager.disconnectServer("10")
        assertEquals(McpConnectionState.DISCONNECTED, manager.getConnectionState("10"))
    }

    @Test
    fun `reconnectServer does nothing when server is disabled`() {
        val server = McpServer(id = 20L, name = "s", connectionType = "stdio", command = "c", args = "", isEnabled = false)
        manager.addServer(server)
        manager.reconnectServer("20")
        assertEquals(McpConnectionState.DISCONNECTED, manager.getConnectionState("20"))
    }
}
