package com.mdrobnak.lalrpop

import com.intellij.idea.IdeaLogger
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.util.Consumer
import io.sentry.*
import io.sentry.protocol.Message
import io.sentry.protocol.SentryId
import java.awt.Component

/**
 * An error reporter that sends errors to Sentry
 */
class LpErrorReporter : ErrorReportSubmitter() {
    // Initialize Sentry as soon as this class is loaded.
    companion object {
        init {
            Sentry.init { options: SentryOptions ->
                options.dsn = "https://d561feacd9a344cbaf6c87f073a5ff24@o1040632.ingest.sentry.io/6009680"
                options.isAttachServerName = false
                options.beforeSend = SentryOptions.BeforeSendCallback(::beforeSend)
            }
        }

        fun beforeSend(event: SentryEvent, hint: Hint): SentryEvent? {
            // Filter out events that are unrelated to this plugin
            val hasPluginFrame = event.exceptions?.any { ex ->
                ex.stacktrace?.frames?.any { frame ->
                    frame.module?.startsWith("com.mdrobnak.lalrpop") == true
                } == true
            } == true
            return if (hasPluginFrame) event else null
        }
    }

    override fun getReportActionText(): String = "Report to Plugin Developer"

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val event = events.firstOrNull() ?: return false

        Sentry.withScope { scope ->
            // Gather some basic data
            val sentryEvent = SentryEvent()
            sentryEvent.level = SentryLevel.ERROR
            sentryEvent.throwable = event.throwable
            sentryEvent.message = additionalInfo?.let { info ->
                Message().apply { message = info }
            }
            sentryEvent.release = "intellij-lalrpop_${pluginDescriptor.version}"
            sentryEvent.setTag("ide.build", ApplicationInfo.getInstance().build.asString())
            sentryEvent.setTag("ide.lastAction", IdeaLogger.ourLastActionId)

            for (attachment in event.attachments) {
                scope.addAttachment(Attachment(attachment.bytes, attachment.path))
            }

            // Send the event and report the result
            val eventId = Sentry.captureEvent(sentryEvent)
            val reportStatus = if (eventId == SentryId.EMPTY_ID) {
                SubmittedReportInfo.SubmissionStatus.FAILED
            } else {
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
            }

            consumer.consume(SubmittedReportInfo(reportStatus))
        }

        return true
    }
}