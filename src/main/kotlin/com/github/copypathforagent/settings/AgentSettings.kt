package com.github.copypathforagent.settings

import com.github.copypathforagent.util.FormatPreset
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "CopyPathForAgentSettings",
    storages = [Storage("CopyPathForAgent.xml")]
)
class AgentSettings : PersistentStateComponent<AgentSettings.State> {

    enum class MultiFileSeparator { SPACE, NEWLINE }

    data class State(
        var showNotification: Boolean = true,
        var notificationDurationSeconds: Int = 3,
        var multiFileSeparator: MultiFileSeparator = MultiFileSeparator.SPACE,
        var formatPreset: FormatPreset = FormatPreset.CLAUDE_CODE,
        var template: String = FormatPreset.CLAUDE_CODE.template
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    val showNotification: Boolean get() = state.showNotification
    val notificationDurationMs: Int get() = state.notificationDurationSeconds.coerceIn(1, 30) * 1000
    val multiFileSeparator: MultiFileSeparator get() = state.multiFileSeparator
    val template: String get() = state.template.ifBlank { state.formatPreset.template }

    companion object {
        fun getInstance(): AgentSettings =
            ApplicationManager.getApplication().getService(AgentSettings::class.java)
    }
}
