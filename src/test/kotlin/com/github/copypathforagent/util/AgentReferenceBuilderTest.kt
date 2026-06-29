package com.github.copypathforagent.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentReferenceBuilderTest {

    @Test
    fun `claude code profile uses generic path and line range variables`() {
        assertEquals(
            "@src/Main.kt#L5-10",
            AgentReferenceBuilder.build("src/Main.kt", startLine = 5, endLine = 10)
        )
    }

    @Test
    fun `claude code profile omits line marker when line range is empty`() {
        assertEquals(
            "@src/Main.kt",
            AgentReferenceBuilder.build("src/Main.kt")
        )
    }

    @Test
    fun `codex app profile uses markdown file link with line range target`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "[Main.kt](/Users/example/project/src/Main.kt:5-10)",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `codex app profile omits line label and target when line is empty`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt"
        )

        assertEquals(
            "[Main.kt](/Users/example/project/src/Main.kt)",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `codex app profile keeps markdown file link target as absolute path`() {
        val context = ReferenceContext(
            relativePath = "src/My Component.kt",
            absolutePath = "/Users/example/My Project/src/My Component.kt",
            fileName = "My Component.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "[My Component.kt](/Users/example/My Project/src/My Component.kt:5-10)",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `codex app profile handles real style java path with selected range`() {
        val context = ReferenceContext(
            relativePath = "df-mdf-prd-productive-feignimpl/src/main/java/com/szlanyou/cloud/productive/feignclient/common/DbToolsVO.java",
            absolutePath = "/Users/zuozhi/workspace/Lanyou/QX_SRM/df-mdf-prd-productive/df-mdf-prd-productive-feignimpl/src/main/java/com/szlanyou/cloud/productive/feignclient/common/DbToolsVO.java",
            fileName = "DbToolsVO.java",
            startLine = 76,
            endLine = 77
        )

        assertEquals(
            "[DbToolsVO.java](/Users/zuozhi/workspace/Lanyou/QX_SRM/df-mdf-prd-productive/df-mdf-prd-productive-feignimpl/src/main/java/com/szlanyou/cloud/productive/feignclient/common/DbToolsVO.java:76-77)",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `custom template can compose minimal variables`() {
        val context = ReferenceContext(
            relativePath = "src/ui/Main.View.kt",
            absolutePath = "/Users/example/project/src/ui/Main.View.kt",
            fileName = "Main.View.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "src/ui | /Users/example/project/src/ui | Main.View.kt | 5 | 10 | 5-10 | file:///Users/example/project/src/ui/Main.View.kt",
            AgentReferenceBuilder.build(
                context,
                "{relativeDirectory} | {absoluteDirectory} | {fileName} | {startLine} | {endLine} | {lineRange} | {fileUri}"
            )
        )
    }

    @Test
    fun `custom template optional sections render only when variable is not empty`() {
        assertEquals(
            "src/Main.kt:5",
            AgentReferenceBuilder.build("src/Main.kt", startLine = 5, template = "{relativePath}{{#startLine}}:{startLine}{{/startLine}}")
        )
        assertEquals(
            "src/Main.kt",
            AgentReferenceBuilder.build("src/Main.kt", template = "{relativePath}{{#startLine}}:{startLine}{{/startLine}}")
        )
    }
}
