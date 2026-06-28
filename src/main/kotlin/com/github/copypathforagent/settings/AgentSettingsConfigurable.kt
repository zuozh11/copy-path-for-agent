package com.github.copypathforagent.settings

import com.github.copypathforagent.util.AgentReferenceBuilder
import com.github.copypathforagent.util.FormatPreset
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class AgentSettingsConfigurable : Configurable {

    private var showNotificationCheckbox: JBCheckBox? = null
    private var durationSpinner: JSpinner? = null
    private var durationRow: Row? = null
    private var multiFileSeparatorCombo: JComboBox<String>? = null
    private var formatPresetCombo: JComboBox<String>? = null
    private var templateArea: JBTextArea? = null

    override fun getDisplayName(): String = "Copy Path for Agent"

    override fun createComponent(): JComponent {
        val settings = AgentSettings.getInstance()

        showNotificationCheckbox = JBCheckBox("Show notification after copy", settings.state.showNotification)
        durationSpinner = JSpinner(SpinnerNumberModel(settings.state.notificationDurationSeconds, 1, 30, 1))

        val separatorOptions = arrayOf("Space", "Newline")
        multiFileSeparatorCombo = JComboBox(separatorOptions).apply {
            selectedIndex = settings.state.multiFileSeparator.ordinal
        }

        formatPresetCombo = JComboBox(FormatPreset.entries.map { it.displayName }.toTypedArray()).apply {
            selectedIndex = settings.state.formatPreset.ordinal
            addActionListener {
                val preset = FormatPreset.entries.getOrNull(selectedIndex) ?: FormatPreset.CLAUDE_CODE
                templateArea?.text = preset.template
            }
        }

        templateArea = JBTextArea(settings.state.template.ifBlank { settings.state.formatPreset.template }, 4, 60).apply {
            lineWrap = false
        }

        val variableHelp = AgentReferenceBuilder.templateVariables.joinToString("<br>") {
            "<code>{${it.name}}</code> - ${it.description}"
        }

        val panel = panel {
            group("Copy Format") {
                row("Preset:") {
                    cell(formatPresetCombo!!)
                        .comment("Choose a preset to fill the template. You can edit the template after selecting a preset.")
                }
                row("Template:") {
                    cell(templateArea!!)
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .comment(variableHelp)
                }
                row("Multiple references separator:") {
                    cell(multiFileSeparatorCombo!!)
                        .comment("Separator between references when copying multiple files or multi-cursor selections")
                }
            }
            group("Notification") {
                row {
                    cell(showNotificationCheckbox!!).onChanged {
                        durationRow?.enabled(it.isSelected)
                    }
                }
                durationRow = row("Notification duration (seconds):") {
                    cell(durationSpinner!!)
                }.enabled(settings.state.showNotification)
            }
            group("Keyboard Shortcut") {
                row {
                    text("Default shortcut: <b>Alt+C</b> (⌥C on macOS)")
                }
                row {
                    button("Configure in Keymap...") {
                        val keymapPanel = com.intellij.openapi.keymap.impl.ui.KeymapPanel()
                        ShowSettingsUtil.getInstance().editConfigurable(
                            null as java.awt.Component?,
                            keymapPanel
                        ) {
                            keymapPanel.selectAction("CopyPathForAgent")
                        }
                    }
                }
            }
        }

        return panel
    }

    override fun isModified(): Boolean {
        val settings = AgentSettings.getInstance()
        return showNotificationCheckbox?.isSelected != settings.state.showNotification
                || (durationSpinner?.value as? Int) != settings.state.notificationDurationSeconds
                || multiFileSeparatorCombo?.selectedIndex != settings.state.multiFileSeparator.ordinal
                || formatPresetCombo?.selectedIndex != settings.state.formatPreset.ordinal
                || templateArea?.text != settings.state.template
    }

    override fun apply() {
        val settings = AgentSettings.getInstance()
        settings.state.showNotification = showNotificationCheckbox?.isSelected ?: true
        settings.state.notificationDurationSeconds = (durationSpinner?.value as? Int) ?: 3
        val separatorIndex = (multiFileSeparatorCombo?.selectedIndex ?: 0).coerceIn(0, AgentSettings.MultiFileSeparator.entries.lastIndex)
        settings.state.multiFileSeparator = AgentSettings.MultiFileSeparator.entries[separatorIndex]
        val presetIndex = (formatPresetCombo?.selectedIndex ?: 0).coerceIn(0, FormatPreset.entries.lastIndex)
        settings.state.formatPreset = FormatPreset.entries[presetIndex]
        settings.state.template = templateArea?.text ?: settings.state.formatPreset.template
    }

    override fun reset() {
        val settings = AgentSettings.getInstance()
        showNotificationCheckbox?.isSelected = settings.state.showNotification
        durationSpinner?.value = settings.state.notificationDurationSeconds
        durationRow?.enabled(settings.state.showNotification)
        multiFileSeparatorCombo?.selectedIndex = settings.state.multiFileSeparator.ordinal
        formatPresetCombo?.selectedIndex = settings.state.formatPreset.ordinal
        templateArea?.text = settings.state.template.ifBlank { settings.state.formatPreset.template }
    }

    override fun disposeUIResources() {
        showNotificationCheckbox = null
        durationSpinner = null
        durationRow = null
        multiFileSeparatorCombo = null
        formatPresetCombo = null
        templateArea = null
    }
}
