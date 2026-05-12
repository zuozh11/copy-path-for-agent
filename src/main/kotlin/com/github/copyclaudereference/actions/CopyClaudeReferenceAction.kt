package com.github.copyclaudereference.actions

import com.github.copyclaudereference.notification.ClaudeNotifier
import com.github.copyclaudereference.settings.ClaudeSettings
import com.github.copyclaudereference.util.ClaudeReferenceBuilder
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import java.awt.datatransfer.StringSelection

class CopyClaudeReferenceAction : AnAction() {

    private fun appendTrailingSpace(reference: String): String {
        return if (ClaudeSettings.getInstance().appendTrailingSpace) "$reference " else reference
    }

    private fun getMultiFileSeparator(): String {
        return when (ClaudeSettings.getInstance().multiFileSeparator) {
            ClaudeSettings.MultiFileSeparator.SPACE -> " "
            ClaudeSettings.MultiFileSeparator.NEWLINE -> "\n"
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null
                && virtualFile != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectDir = project.guessProjectDir() ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)

        // Editor with selection or multi-caret → file with line numbers
        if (editor != null) {
            val carets = editor.caretModel.allCarets
            val hasAnySelection = carets.any { it.hasSelection() }
            val isMultiCaret = carets.size > 1

            if (hasAnySelection || isMultiCaret) {
                val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
                val relativePath = VfsUtilCore.getRelativePath(virtualFile, projectDir) ?: virtualFile.name
                val document = editor.document

                val sortedCarets = carets.sortedBy { it.offset }

                val references = sortedCarets.map { caret ->
                    if (caret.hasSelection()) {
                        val startLine = document.getLineNumber(caret.selectionStart) + 1
                        var endLine = document.getLineNumber(caret.selectionEnd) + 1

                        // Edge case: selection ends at the very beginning of the next line
                        if (caret.selectionEnd == document.getLineStartOffset(endLine - 1) && endLine > startLine) {
                            endLine--
                        }
                        ClaudeReferenceBuilder.build(relativePath, startLine, endLine)
                    } else {
                        // Caret without selection: use cursor's line number
                        val line = document.getLineNumber(caret.offset) + 1
                        ClaudeReferenceBuilder.build(relativePath, line, line)
                    }
                }

                val combined = references.joinToString(getMultiFileSeparator())
                val output = appendTrailingSpace(combined)
                CopyPasteManager.getInstance().setContents(StringSelection(output))

                if (references.size > 1) {
                    ClaudeNotifier.notify(project, "${references.size} references copied")
                } else {
                    ClaudeNotifier.notify(project, references.first())
                }
                return
            }
        }

        // Multi-file/folder selection from project tree or single file without selection
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val selectedFiles = files?.takeIf { it.size > 1 }

        if (selectedFiles != null) {
            val references = selectedFiles.mapNotNull { file ->
                val path = VfsUtilCore.getRelativePath(file, projectDir) ?: file.name
                val resolvedPath = if (file.isDirectory) "$path/" else path
                ClaudeReferenceBuilder.build(resolvedPath)
            }
            val combined = references.joinToString(getMultiFileSeparator())
            val output = appendTrailingSpace(combined)
            CopyPasteManager.getInstance().setContents(StringSelection(output))
            ClaudeNotifier.notify(project, "${references.size} paths copied")
        } else {
            val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
            val relativePath = VfsUtilCore.getRelativePath(virtualFile, projectDir) ?: virtualFile.name
            val resolvedPath = if (virtualFile.isDirectory) "$relativePath/" else relativePath
            val reference = ClaudeReferenceBuilder.build(resolvedPath)
            val output = appendTrailingSpace(reference)
            CopyPasteManager.getInstance().setContents(StringSelection(output))
            ClaudeNotifier.notify(project, reference)
        }
    }
}
