package com.github.copypathforagent.util

data class ReferenceContext(
    val relativePath: String,
    val absolutePath: String = relativePath,
    val fileName: String = relativePath.substringAfterLast('/'),
    val isDirectory: Boolean = false,
    val startLine: Int? = null,
    val endLine: Int? = null
)

enum class FormatPreset(val displayName: String, val template: String) {
    CLAUDE_CODE("claudecode", "{claudeReference}"),
    CODEX("codex", "[{codexLabel}]({codexTarget})")
}

data class TemplateVariable(
    val name: String,
    val description: String
)

object AgentReferenceBuilder {

    val templateVariables = listOf(
        TemplateVariable("relativePath", "Project-relative path exactly as provided by the IDE."),
        TemplateVariable("absolutePath", "Absolute local path exactly as provided by the IDE."),
        TemplateVariable("relativePathWithDirectorySlash", "Project-relative path plus {directorySlash}."),
        TemplateVariable("absolutePathWithDirectorySlash", "Absolute local path plus {directorySlash}."),
        TemplateVariable("fileName", "The file or folder name only."),
        TemplateVariable("isDirectory", "true for folders, false for files."),
        TemplateVariable("directorySlash", "A slash for folders, otherwise empty."),
        TemplateVariable("startLine", "The 1-based start line, or empty when no line is selected."),
        TemplateVariable("endLine", "The 1-based end line, or empty when no line is selected."),
        TemplateVariable("lineRange", "The selected line or line range, such as 5 or 5-10."),
        TemplateVariable("lineText", "Human-readable line text, such as line 5 or lines 5-10."),
        TemplateVariable("claudeLineSuffix", "Claude Code line suffix, such as #L5 or #L5-10."),
        TemplateVariable("claudeReference", "Complete Claude Code reference, such as @src/App.kt#L5."),
        TemplateVariable("claudeReferenceWithDirectorySlash", "Claude Code reference using relativePathWithDirectorySlash."),
        TemplateVariable("codexLabel", "Markdown link label for Codex, such as App.kt:5-10."),
        TemplateVariable("codexTarget", "Markdown link target for Codex, using the absolute path and start line."),
        TemplateVariable("codexTargetWithDirectorySlash", "Markdown link target for Codex using absolutePathWithDirectorySlash."),
        TemplateVariable("space", "A literal space, useful when you want a trailing space.")
    )

    fun build(
        relativePath: String,
        absolutePath: String = relativePath,
        fileName: String = relativePath.substringAfterLast('/'),
        isDirectory: Boolean = false,
        startLine: Int? = null,
        endLine: Int? = null,
        template: String = FormatPreset.CLAUDE_CODE.template
    ): String {
        return build(
            ReferenceContext(
                relativePath = relativePath,
                absolutePath = absolutePath,
                fileName = fileName,
                isDirectory = isDirectory,
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
            "relativePathWithDirectorySlash" to context.relativePath.withDirectorySlash(context),
            "absolutePathWithDirectorySlash" to context.absolutePath.withDirectorySlash(context),
            "fileName" to context.fileName,
            "isDirectory" to context.isDirectory.toString(),
            "directorySlash" to directorySlash(context),
            "startLine" to context.startLine?.toString().orEmpty(),
            "endLine" to context.endLine?.toString().orEmpty(),
            "lineRange" to lineRange(context),
            "lineText" to lineText(context),
            "claudeLineSuffix" to claudeLineSuffix(context),
            "claudeReference" to claudeReference(context),
            "claudeReferenceWithDirectorySlash" to claudeReference(context, includeDirectorySlash = true),
            "codexLabel" to codexLabel(context),
            "codexTarget" to codexTarget(context),
            "codexTargetWithDirectorySlash" to codexTarget(context, includeDirectorySlash = true),
            "space" to " "
        )

        return variables.entries.fold(template) { rendered, (name, value) ->
            rendered.replace("{$name}", value)
        }
    }

    private fun claudeLineSuffix(context: ReferenceContext): String {
        val startLine = context.startLine ?: return ""
        val endLine = context.endLine
        return if (endLine == null || endLine == startLine) "#L$startLine" else "#L$startLine-$endLine"
    }

    private fun claudeReference(context: ReferenceContext, includeDirectorySlash: Boolean = false): String {
        val path = if (includeDirectorySlash) context.relativePath.withDirectorySlash(context) else context.relativePath
        val inner = path + claudeLineSuffix(context)
        val escaped = inner.replace("\"", "\\\"")
        return if (escaped.any { it.isWhitespace() }) "@\"$escaped\"" else "@$escaped"
    }

    private fun codexLabel(context: ReferenceContext): String {
        val range = lineRange(context)
        return if (range.isEmpty()) context.fileName else "${context.fileName}:$range"
    }

    private fun codexTarget(context: ReferenceContext, includeDirectorySlash: Boolean = false): String {
        val startLine = context.startLine
        val path = if (includeDirectorySlash) context.absolutePath.withDirectorySlash(context) else context.absolutePath
        val target = if (startLine == null) path else "$path:$startLine"
        return if (target.any { it.isWhitespace() || it == '(' || it == ')' }) "<$target>" else target
    }

    private fun directorySlash(context: ReferenceContext): String =
        if (context.isDirectory) "/" else ""

    private fun String.withDirectorySlash(context: ReferenceContext): String {
        val slash = directorySlash(context)
        return if (slash.isNotEmpty() && !endsWith(slash)) "$this$slash" else this
    }

    private fun lineRange(context: ReferenceContext): String {
        val startLine = context.startLine ?: return ""
        val endLine = context.endLine
        return if (endLine == null || endLine == startLine) "$startLine" else "$startLine-$endLine"
    }

    private fun lineText(context: ReferenceContext): String {
        val range = lineRange(context)
        if (range.isEmpty()) return ""
        return if ('-' in range) "lines $range" else "line $range"
    }
}
