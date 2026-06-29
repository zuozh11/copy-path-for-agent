package com.github.copypathforagent.status

import com.github.copypathforagent.AgentBundle
import com.github.copypathforagent.settings.AgentSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class AgentProfileStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = AgentProfileStatusBarWidget.ID

    override fun getDisplayName(): String = AgentBundle.message("status.profile.displayName")

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget =
        AgentProfileStatusBarWidget()

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun isEnabledByDefault(): Boolean = true
}

@Suppress("OVERRIDE_DEPRECATION")
private class AgentProfileStatusBarWidget : StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
    private var statusBar: StatusBar? = null

    override fun ID(): String = ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getSelectedValue(): String =
        AgentBundle.message("status.profile.text", AgentSettings.getInstance().activeProfile.name)

    override fun getTooltipText(): String? =
        AgentBundle.message("status.profile.popupTitle")

    override fun getMaxValue(): String =
        AgentBundle.message("status.profile.text", AgentSettings.getInstance().profileNames().maxByOrNull { it.length }.orEmpty())

    override fun getPopupStep(): ListPopup {
        val settings = AgentSettings.getInstance()
        val profiles = settings.state.profiles.map { it.copy() }
        val step = object : BaseListPopupStep<AgentSettings.Profile>(
            AgentBundle.message("status.profile.popupTitle"),
            profiles
        ) {
            override fun getTextFor(value: AgentSettings.Profile): String = value.name

            override fun onChosen(selectedValue: AgentSettings.Profile, finalChoice: Boolean): PopupStep<*>? {
                settings.setActiveProfile(selectedValue.id)
                statusBar?.updateWidget(ID)
                return FINAL_CHOICE
            }
        }
        return JBPopupFactory.getInstance().createListPopup(step)
    }

    companion object {
        const val ID = "CopyPathForAgent.Profile"
    }
}
