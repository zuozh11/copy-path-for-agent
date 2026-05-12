package com.github.copyclaudereference.util

object ClaudeReferenceBuilder {

    fun build(relativePath: String, startLine: Int? = null, endLine: Int? = null): String {
        val suffix = when {
            startLine == null -> ""
            endLine == null || endLine == startLine -> "#L$startLine"
            else -> "#L$startLine-$endLine"
        }
        val inner = "$relativePath$suffix"
        return if (inner.contains(' ')) "@\"$inner\"" else "@$inner"
    }
}
