package com.github.copypathforagent.actions

import com.github.copypathforagent.AgentBundle
import com.github.copypathforagent.notification.AgentNotifier
import com.github.copypathforagent.settings.AgentSettings
import com.github.copypathforagent.util.AgentReferenceBuilder
import com.github.copypathforagent.util.ReferenceContext
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtilCore
import java.awt.datatransfer.StringSelection

class CopyAgentReferenceAction : AnAction() {

    private fun getMultiFileSeparator(): String {
        return when (AgentSettings.getInstance().multiFileSeparator) {
            AgentSettings.MultiFileSeparator.SPACE -> " "
            AgentSettings.MultiFileSeparator.NEWLINE -> "\n"
        }
    }

    private fun buildReference(
        file: VirtualFile,
        projectDir: VirtualFile,
        startLine: Int? = null,
        endLine: Int? = null
    ): String {
        val relativePath = VfsUtilCore.getRelativePath(file, projectDir) ?: file.name
        val absolutePath = file.path
        val context = ReferenceContext(
            relativePath = relativePath,
            absolutePath = absolutePath,
            fileName = file.name,
            startLine = startLine,
            endLine = endLine
        )
        return AgentReferenceBuilder.build(context, AgentSettings.getInstance().template)
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
                        buildReference(virtualFile, projectDir, startLine, endLine)
                    } else {
                        // Caret without selection: use cursor's line number
                        val line = document.getLineNumber(caret.offset) + 1
                        buildReference(virtualFile, projectDir, line, line)
                    }
                }

                val combined = references.joinToString(getMultiFileSeparator())
                CopyPasteManager.getInstance().setContents(StringSelection(combined))

                if (references.size > 1) {
                    AgentNotifier.notify(project, AgentBundle.message("notification.references", references.size))
                } else {
                    AgentNotifier.notify(project, references.first())
                }
                return
            }
        }

        // Multi-file/folder selection from project tree or single file without selection
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val selectedFiles = files?.takeIf { it.size > 1 }

        if (selectedFiles != null) {
            val references = selectedFiles.mapNotNull { file ->
                buildReference(file, projectDir)
            }
            val combined = references.joinToString(getMultiFileSeparator())
            CopyPasteManager.getInstance().setContents(StringSelection(combined))
            AgentNotifier.notify(project, AgentBundle.message("notification.paths", references.size))
        } else {
            val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
            val reference = buildReference(virtualFile, projectDir)
            CopyPasteManager.getInstance().setContents(StringSelection(reference))
            AgentNotifier.notify(project, reference)
        }
    }
}
