package com.github.copypathforagent.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentReferenceBuilderTest {

    @Test
    fun `claudecode preset file path only`() {
        assertEquals("@src/Main.kt", AgentReferenceBuilder.build("src/Main.kt"))
    }

    @Test
    fun `claudecode preset single line`() {
        assertEquals("@src/Main.kt#L5", AgentReferenceBuilder.build("src/Main.kt", startLine = 5, endLine = 5))
    }

    @Test
    fun `claudecode preset multiple lines`() {
        assertEquals("@src/Main.kt#L5-10", AgentReferenceBuilder.build("src/Main.kt", startLine = 5, endLine = 10))
    }

    @Test
    fun `claudecode preset path with spaces no line`() {
        assertEquals("@\"my component.tsx\"", AgentReferenceBuilder.build("my component.tsx"))
    }

    @Test
    fun `claudecode preset path with spaces range`() {
        assertEquals(
            "@\"my component.tsx#L5-10\"",
            AgentReferenceBuilder.build("my component.tsx", startLine = 5, endLine = 10)
        )
    }

    @Test
    fun `claudecode preset does not add directory slash by default`() {
        val context = ReferenceContext(
            relativePath = "src/main",
            absolutePath = "/Users/example/project/src/main",
            fileName = "main",
            isDirectory = true
        )

        assertEquals("@src/main", AgentReferenceBuilder.build(context, FormatPreset.CLAUDE_CODE.template))
    }

    @Test
    fun `custom template can add directory slash`() {
        val context = ReferenceContext(
            relativePath = "src/main",
            absolutePath = "/Users/example/project/src/main",
            fileName = "main",
            isDirectory = true
        )

        assertEquals("@src/main/", AgentReferenceBuilder.build(context, "@{relativePathWithDirectorySlash}"))
    }

    @Test
    fun `custom claude variable can add directory slash with quotes`() {
        val context = ReferenceContext(
            relativePath = "src/my components",
            absolutePath = "/Users/example/project/src/my components",
            fileName = "my components",
            isDirectory = true
        )

        assertEquals("@\"src/my components/\"", AgentReferenceBuilder.build(context, "{claudeReferenceWithDirectorySlash}"))
    }

    @Test
    fun `codex preset file path only`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt"
        )

        assertEquals("[Main.kt](/Users/example/project/src/Main.kt)", AgentReferenceBuilder.build(context, FormatPreset.CODEX.template))
    }

    @Test
    fun `codex preset single line uses absolute path with start line`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt",
            startLine = 5,
            endLine = 5
        )

        assertEquals("[Main.kt:5](/Users/example/project/src/Main.kt:5)", AgentReferenceBuilder.build(context, FormatPreset.CODEX.template))
    }

    @Test
    fun `codex preset range labels range and links to start line`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals("[Main.kt:5-10](/Users/example/project/src/Main.kt:5)", AgentReferenceBuilder.build(context, FormatPreset.CODEX.template))
    }

    @Test
    fun `codex preset wraps target with angle brackets when path has spaces`() {
        val context = ReferenceContext(
            relativePath = "src/My Component.kt",
            absolutePath = "/Users/example/My Project/src/My Component.kt",
            fileName = "My Component.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "[My Component.kt:5-10](</Users/example/My Project/src/My Component.kt:5>)",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `custom template can add trailing space through variable`() {
        assertEquals(
            "src/Main.kt line 5 ",
            AgentReferenceBuilder.build("src/Main.kt", startLine = 5, endLine = 5, template = "{relativePath} {lineText}{space}")
        )
    }
}
