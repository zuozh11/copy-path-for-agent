package com.github.copypathforagent.settings

import com.github.copypathforagent.AgentBundle
import com.github.copypathforagent.util.AgentReferenceBuilder
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import java.util.MissingResourceException
import java.util.UUID
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class AgentSettingsConfigurable : Configurable {

    private var showNotificationCheckbox: JBCheckBox? = null
    private var durationSpinner: JSpinner? = null
    private var durationRow: Row? = null
    private var profileCombo: JComboBox<String>? = null
    private var multiFileSeparatorCombo: JComboBox<String>? = null
    private var templateArea: JBTextArea? = null
    private var profiles: MutableList<AgentSettings.Profile> = mutableListOf()
    private var selectedProfileIndex: Int = 0
    private var loadingProfile = false

    override fun getDisplayName(): String = AgentBundle.message("settings.displayName")

    override fun createComponent(): JComponent {
        val settings = AgentSettings.getInstance()
        profiles = settings.state.profiles.map { it.copy() }.toMutableList()
        selectedProfileIndex = profiles.indexOfFirst { it.id == settings.state.activeProfileId }.takeIf { it >= 0 } ?: 0

        showNotificationCheckbox = JBCheckBox(
            AgentBundle.message("settings.notification.show"),
            settings.state.showNotification
        )
        durationSpinner = JSpinner(SpinnerNumberModel(settings.state.notificationDurationSeconds, 1, 30, 1))

        profileCombo = JComboBox(profiles.map { it.name }.toTypedArray()).apply {
            selectedIndex = selectedProfileIndex
            addActionListener {
                if (loadingProfile) return@addActionListener
                val newIndex = selectedIndex
                if (newIndex == selectedProfileIndex || newIndex !in profiles.indices) return@addActionListener
                saveSelectedProfile()
                selectedProfileIndex = newIndex
                loadSelectedProfile()
            }
        }

        val separatorOptions = arrayOf(
            AgentBundle.message("settings.separator.space"),
            AgentBundle.message("settings.separator.newline")
        )
        multiFileSeparatorCombo = JComboBox(separatorOptions)

        templateArea = JBTextArea("", 8, 72).apply {
            lineWrap = false
        }
        loadSelectedProfile()

        val variableHelp = AgentReferenceBuilder.templateVariables.joinToString("<br>") {
            "<code>{${it.name}}</code> - ${templateVariableDescription(it.name, it.description)}"
        }

        val panel = panel {
            group(AgentBundle.message("settings.group.profile")) {
                row(AgentBundle.message("settings.profile")) {
                    cell(profileCombo!!)
                        .comment(AgentBundle.message("settings.profile.comment"))
                    button(AgentBundle.message("settings.profile.add")) { addProfile() }
                    button(AgentBundle.message("settings.profile.rename")) { renameProfile() }
                    button(AgentBundle.message("settings.profile.delete")) { deleteProfile() }
                }
            }
            group(AgentBundle.message("settings.group.copyFormat")) {
                row(AgentBundle.message("settings.template")) {
                    cell(templateArea!!)
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .comment(variableHelp)
                }
                row(AgentBundle.message("settings.separator")) {
                    cell(multiFileSeparatorCombo!!)
                        .comment(AgentBundle.message("settings.separator.comment"))
                }
            }
            group(AgentBundle.message("settings.group.notification")) {
                row {
                    cell(showNotificationCheckbox!!).onChanged {
                        durationRow?.enabled(it.isSelected)
                    }
                }
                durationRow = row(AgentBundle.message("settings.notification.duration")) {
                    cell(durationSpinner!!)
                }.enabled(settings.state.showNotification)
            }
            group(AgentBundle.message("settings.group.shortcut")) {
                row {
                    text(AgentBundle.message("settings.shortcut.default"))
                }
                row {
                    button(AgentBundle.message("settings.shortcut.configure")) {
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
        val uiProfiles = currentProfilesSnapshot()
        return showNotificationCheckbox?.isSelected != settings.state.showNotification
                || (durationSpinner?.value as? Int) != settings.state.notificationDurationSeconds
                || uiProfiles != settings.state.profiles
                || uiProfiles.getOrNull(selectedProfileIndex)?.id != settings.state.activeProfileId
    }

    override fun apply() {
        val settings = AgentSettings.getInstance()
        val uiProfiles = currentProfilesSnapshot()

        settings.state.showNotification = showNotificationCheckbox?.isSelected ?: true
        settings.state.notificationDurationSeconds = (durationSpinner?.value as? Int) ?: 3
        settings.state.profiles = uiProfiles.toMutableList()
        settings.state.activeProfileId = uiProfiles.getOrNull(selectedProfileIndex)?.id ?: uiProfiles.first().id

        WindowManager.getInstance().allProjectFrames.forEach { frame ->
            frame.statusBar?.updateWidget("CopyPathForAgent.Profile")
        }
    }

    override fun reset() {
        val settings = AgentSettings.getInstance()
        profiles = settings.state.profiles.map { it.copy() }.toMutableList()
        selectedProfileIndex = profiles.indexOfFirst { it.id == settings.state.activeProfileId }.takeIf { it >= 0 } ?: 0
        showNotificationCheckbox?.isSelected = settings.state.showNotification
        durationSpinner?.value = settings.state.notificationDurationSeconds
        durationRow?.enabled(settings.state.showNotification)
        refreshProfileCombo(selectedProfileIndex)
        loadSelectedProfile()
    }

    override fun disposeUIResources() {
        showNotificationCheckbox = null
        durationSpinner = null
        durationRow = null
        profileCombo = null
        multiFileSeparatorCombo = null
        templateArea = null
        profiles = mutableListOf()
    }

    private fun addProfile() {
        saveSelectedProfile()
        val name = Messages.showInputDialog(
            AgentBundle.message("settings.profile.add.message"),
            AgentBundle.message("settings.profile.add.title"),
            null
        )?.trim().orEmpty()
        if (name.isBlank()) return

        val base = profiles.getOrNull(selectedProfileIndex)
            ?: AgentSettings.defaultProfiles().first()
        profiles.add(base.copy(id = UUID.randomUUID().toString(), name = uniqueProfileName(name)))
        selectedProfileIndex = profiles.lastIndex
        refreshProfileCombo(selectedProfileIndex)
        loadSelectedProfile()
    }

    private fun renameProfile() {
        saveSelectedProfile()
        val current = profiles.getOrNull(selectedProfileIndex) ?: return
        val name = Messages.showInputDialog(
            AgentBundle.message("settings.profile.rename.message"),
            AgentBundle.message("settings.profile.rename.title"),
            null,
            current.name,
            null
        )?.trim().orEmpty()
        if (name.isBlank()) return

        current.name = uniqueProfileName(name, current.id)
        refreshProfileCombo(selectedProfileIndex)
    }

    private fun deleteProfile() {
        if (profiles.size <= 1) {
            Messages.showErrorDialog(
                AgentBundle.message("settings.profile.delete.last"),
                AgentBundle.message("settings.profile.delete.last.title")
            )
            return
        }

        profiles.removeAt(selectedProfileIndex)
        selectedProfileIndex = selectedProfileIndex.coerceAtMost(profiles.lastIndex)
        refreshProfileCombo(selectedProfileIndex)
        loadSelectedProfile()
    }

    private fun currentProfilesSnapshot(): List<AgentSettings.Profile> {
        saveSelectedProfile()
        return profiles.map { it.copy() }
    }

    private fun saveSelectedProfile() {
        val profile = profiles.getOrNull(selectedProfileIndex) ?: return
        profile.template = templateArea?.text?.takeIf { it.isNotBlank() } ?: profile.formatPreset.template
        val separatorIndex = (multiFileSeparatorCombo?.selectedIndex ?: 0)
            .coerceIn(0, AgentSettings.MultiFileSeparator.entries.lastIndex)
        profile.multiFileSeparator = AgentSettings.MultiFileSeparator.entries[separatorIndex]
    }

    private fun loadSelectedProfile() {
        val profile = profiles.getOrNull(selectedProfileIndex) ?: return
        loadingProfile = true
        try {
            templateArea?.text = profile.template.ifBlank { profile.formatPreset.template }
            multiFileSeparatorCombo?.selectedIndex = profile.multiFileSeparator.ordinal
        } finally {
            loadingProfile = false
        }
    }

    private fun refreshProfileCombo(selectIndex: Int) {
        loadingProfile = true
        try {
            profileCombo?.removeAllItems()
            profiles.forEach { profileCombo?.addItem(it.name) }
            profileCombo?.selectedIndex = selectIndex.coerceIn(0, profiles.lastIndex)
        } finally {
            loadingProfile = false
        }
    }

    private fun uniqueProfileName(name: String, currentId: String? = null): String {
        val existing = profiles
            .filterNot { it.id == currentId }
            .map { it.name }
            .toSet()
        if (name !in existing) return name

        var index = 2
        while ("$name $index" in existing) {
            index++
        }
        return "$name $index"
    }

    private fun templateVariableDescription(name: String, fallback: String): String =
        try {
            AgentBundle.message("template.variable.$name")
        } catch (_: MissingResourceException) {
            fallback
        }
}
