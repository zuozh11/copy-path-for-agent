package com.github.copypathforagent.notification

import com.github.copypathforagent.settings.AgentSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit

object AgentNotifier {

    fun notify(project: Project?, reference: String) {
        val settings = AgentSettings.getInstance()
        if (!settings.showNotification) return

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Copy Path for Agent")
            .createNotification(
                "Copied Path for Agent",
                reference,
                NotificationType.INFORMATION
            )
        notification.notify(project)

        AppExecutorUtil.getAppScheduledExecutorService().schedule(
            { ApplicationManager.getApplication().invokeLater { notification.expire() } },
            settings.notificationDurationMs.toLong(),
            TimeUnit.MILLISECONDS
        )
    }
}
