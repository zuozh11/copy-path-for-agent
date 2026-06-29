package com.github.copypathforagent.util

import java.net.URI

data class ReferenceContext(
    val relativePath: String,
    val absolutePath: String = relativePath,
    val fileName: String = relativePath.substringAfterLast('/'),
    val startLine: Int? = null,
    val endLine: Int? = null
)

enum class FormatPreset(val displayName: String, val template: String) {
    CLAUDE_CODE("Claude Code", "@{relativePath}{{#lineRange}}#L{lineRange}{{/lineRange}}"),
    CODEX("Codex App", "{fileUri}{{#startLine}}#L{startLine}{{/startLine}}")
}

data class TemplateVariable(
    val name: String,
    val description: String
)

object AgentReferenceBuilder {

    val templateVariables = listOf(
        TemplateVariable("relativePath", "Project-relative path exactly as provided by the IDE."),
        TemplateVariable("absolutePath", "Absolute local path exactly as provided by the IDE."),
        TemplateVariable("relativeDirectory", "Project-relative parent directory, or empty at project root."),
        TemplateVariable("absoluteDirectory", "Absolute parent directory, or empty when unavailable."),
        TemplateVariable("fileName", "The file or folder name only."),
        TemplateVariable("startLine", "The 1-based start line, or empty when no line is selected."),
        TemplateVariable("endLine", "The 1-based end line, or empty when no line is selected."),
        TemplateVariable("lineRange", "The selected line or line range with a hyphen, such as 5 or 5-10."),
        TemplateVariable("fileUri", "Local file URI, such as file:///Users/me/project/src/App.kt.")
    )

    fun build(
        relativePath: String,
        absolutePath: String = relativePath,
        fileName: String = relativePath.substringAfterLast('/'),
        startLine: Int? = null,
        endLine: Int? = null,
        template: String = FormatPreset.CLAUDE_CODE.template
    ): String {
        return build(
            ReferenceContext(
                relativePath = relativePath,
                absolutePath = absolutePath,
                fileName = fileName,
                startLine = startLine,
                endLine = endLine
            ),
            template
        )
    }

    fun build(context: ReferenceContext, template: String): String {
        val variables = mapOf(
            "relativePath" to context.relativePath,
            "absolutePath" to context.absolutePath,
            "relativeDirectory" to context.relativePath.parentPath(),
            "absoluteDirectory" to context.absolutePath.parentPath(),
            "fileName" to context.fileName,
            "startLine" to context.startLine?.toString().orEmpty(),
            "endLine" to context.endLine?.toString().orEmpty(),
            "lineRange" to lineRange(context),
            "fileUri" to fileUri(context)
        )

        val withOptionalSections = optionalSectionRegex.replace(template) { match ->
            val name = match.groupValues[1]
            val body = match.groupValues[2]
            if (variables[name].orEmpty().isNotEmpty()) body else ""
        }

        return variables.entries.fold(withOptionalSections) { rendered, (name, value) ->
            rendered.replace("{$name}", value)
        }
    }

    private fun fileUri(context: ReferenceContext): String =
        context.absolutePath.let { path ->
            if (path.startsWith("/")) URI("file", "", path, null).toASCIIString() else path
        }

    private fun lineRange(context: ReferenceContext): String {
        val startLine = context.startLine ?: return ""
        val endLine = context.endLine
        return if (endLine == null || endLine == startLine) "$startLine" else "$startLine-$endLine"
    }

    private fun String.parentPath(): String =
        substringBeforeLast('/', "")

    private val optionalSectionRegex = Regex(
        """\{\{#([A-Za-z][A-Za-z0-9]*)}}(.*?)\{\{/\1}}""",
        RegexOption.DOT_MATCHES_ALL
    )
}
