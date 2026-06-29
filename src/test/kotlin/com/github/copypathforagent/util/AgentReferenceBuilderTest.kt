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
    fun `codex app profile uses file uri and start line variables`() {
        val context = ReferenceContext(
            relativePath = "src/Main.kt",
            absolutePath = "/Users/example/project/src/Main.kt",
            fileName = "Main.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "file:///Users/example/project/src/Main.kt#L5",
            AgentReferenceBuilder.build(context, FormatPreset.CODEX.template)
        )
    }

    @Test
    fun `codex app profile encodes spaces in file uri`() {
        val context = ReferenceContext(
            relativePath = "src/My Component.kt",
            absolutePath = "/Users/example/My Project/src/My Component.kt",
            fileName = "My Component.kt",
            startLine = 5,
            endLine = 10
        )

        assertEquals(
            "file:///Users/example/My%20Project/src/My%20Component.kt#L5",
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
            "file:///Users/zuozhi/workspace/Lanyou/QX_SRM/df-mdf-prd-productive/df-mdf-prd-productive-feignimpl/src/main/java/com/szlanyou/cloud/productive/feignclient/common/DbToolsVO.java#L76",
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
}
