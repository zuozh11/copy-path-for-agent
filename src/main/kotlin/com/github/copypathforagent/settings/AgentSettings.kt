package com.github.copypathforagent.settings

import com.github.copypathforagent.util.FormatPreset
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.util.UUID

@State(
    name = "CopyPathForAgentSettings",
    storages = [Storage("CopyPathForAgent.xml")]
)
class AgentSettings : PersistentStateComponent<AgentSettings.State> {

    enum class MultiFileSeparator { SPACE, NEWLINE }

    data class Profile(
        var id: String = UUID.randomUUID().toString(),
        var name: String = "",
        var formatPreset: FormatPreset = FormatPreset.CLAUDE_CODE,
        var template: String = FormatPreset.CLAUDE_CODE.template,
        var multiFileSeparator: MultiFileSeparator = MultiFileSeparator.SPACE
    )

    data class State(
        var showNotification: Boolean = true,
        var notificationDurationSeconds: Int = 3,
        var activeProfileId: String = "",
        var profiles: MutableList<Profile> = mutableListOf(),
        var legacyMigrated: Boolean = false,
        var profileSchemaVersion: Int = 0,

        // Kept so existing settings XML can migrate cleanly from 1.0.0.
        var multiFileSeparator: MultiFileSeparator = MultiFileSeparator.SPACE,
        var formatPreset: FormatPreset = FormatPreset.CLAUDE_CODE,
        var template: String = FormatPreset.CLAUDE_CODE.template
    )

    private var state = State()

    init {
        normalizeState(state)
    }

    override fun getState(): State {
        normalizeState(state)
        return state
    }

    override fun loadState(state: State) {
        this.state = state
        normalizeState(this.state)
    }

    val showNotification: Boolean get() = state.showNotification
    val notificationDurationMs: Int get() = state.notificationDurationSeconds.coerceIn(1, 30) * 1000
    val activeProfile: Profile
        get() {
            normalizeState(state)
            return state.profiles.firstOrNull { it.id == state.activeProfileId }
                ?: state.profiles.first()
        }
    val multiFileSeparator: MultiFileSeparator get() = activeProfile.multiFileSeparator
    val template: String get() = activeProfile.template.ifBlank { activeProfile.formatPreset.template }

    fun setActiveProfile(profileId: String) {
        normalizeState(state)
        if (state.profiles.any { it.id == profileId }) {
            state.activeProfileId = profileId
            syncLegacyFields()
        }
    }

    fun profileNames(): List<String> {
        normalizeState(state)
        return state.profiles.map { it.name }
    }

    private fun normalizeState(state: State) {
        if (state.profiles.isEmpty()) {
            val defaults = defaultProfiles().toMutableList()
            val migratedProfile = Profile(
                id = state.formatPreset.profileId,
                name = state.formatPreset.displayName,
                formatPreset = state.formatPreset,
                template = state.template.ifBlank { state.formatPreset.template },
                multiFileSeparator = state.multiFileSeparator
            )
            val index = defaults.indexOfFirst { it.id == migratedProfile.id }
            if (index >= 0) {
                defaults[index] = migratedProfile
            } else {
                defaults.add(migratedProfile)
            }
            state.profiles = defaults
            state.activeProfileId = migratedProfile.id
            state.legacyMigrated = true
        }

        if (state.profileSchemaVersion < CURRENT_PROFILE_SCHEMA_VERSION) {
            val defaultsById = defaultProfiles().associateBy { it.id }
            state.profiles.forEach { profile ->
                val defaultProfile = defaultsById[profile.id] ?: return@forEach
                profile.name = defaultProfile.name
                profile.template = defaultProfile.template
                profile.multiFileSeparator = defaultProfile.multiFileSeparator
                profile.formatPreset = defaultProfile.formatPreset
            }
            state.profileSchemaVersion = CURRENT_PROFILE_SCHEMA_VERSION
        }

        state.profiles.forEach { profile ->
            if (profile.id.isBlank()) profile.id = UUID.randomUUID().toString()
            if (profile.name.isBlank()) profile.name = profile.formatPreset.displayName
            if (profile.template.isBlank()) profile.template = profile.formatPreset.template
        }

        if (state.activeProfileId.isBlank() || state.profiles.none { it.id == state.activeProfileId }) {
            state.activeProfileId = state.profiles.first().id
        }
        syncLegacyFields()
    }

    private fun syncLegacyFields() {
        val profile = state.profiles.firstOrNull { it.id == state.activeProfileId } ?: return
        state.multiFileSeparator = profile.multiFileSeparator
        state.formatPreset = profile.formatPreset
        state.template = profile.template
    }

    companion object {
        private const val CURRENT_PROFILE_SCHEMA_VERSION = 6

        val FormatPreset.profileId: String
            get() = when (this) {
                FormatPreset.CLAUDE_CODE -> "builtin-claudecode"
                FormatPreset.CODEX -> "builtin-codex-app"
            }

        fun defaultProfiles(): List<Profile> = listOf(
            Profile(
                id = FormatPreset.CLAUDE_CODE.profileId,
                name = FormatPreset.CLAUDE_CODE.displayName,
                formatPreset = FormatPreset.CLAUDE_CODE,
                template = FormatPreset.CLAUDE_CODE.template,
                multiFileSeparator = MultiFileSeparator.SPACE
            ),
            Profile(
                id = FormatPreset.CODEX.profileId,
                name = FormatPreset.CODEX.displayName,
                formatPreset = FormatPreset.CODEX,
                template = FormatPreset.CODEX.template,
                multiFileSeparator = MultiFileSeparator.NEWLINE
            )
        )

        fun getInstance(): AgentSettings =
            ApplicationManager.getApplication().getService(AgentSettings::class.java)
    }
}
