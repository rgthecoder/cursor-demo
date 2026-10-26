package com.personal.tvlauncher.a11y

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicReference

/**
 * Reads visible text from the active window for personal-use screen content access.
 * Must be enabled once in TV Settings → Accessibility.
 */
class LauncherAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val text = collectVisibleText(root)
        root.recycle()
        if (text.isNotBlank()) {
            latestScreenText.set(text)
        }
    }

    override fun onInterrupt() {
        // No-op for v1
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    fun captureScreenText(): String {
        val root = rootInActiveWindow ?: return latestScreenText.get().orEmpty()
        return try {
            val text = collectVisibleText(root)
            if (text.isNotBlank()) {
                latestScreenText.set(text)
            }
            text.ifBlank { latestScreenText.get().orEmpty() }
        } finally {
            root.recycle()
        }
    }

    private fun collectVisibleText(root: AccessibilityNodeInfo): String {
        val parts = LinkedHashSet<String>()
        walk(root, parts)
        return parts.joinToString("\n")
    }

    private fun walk(node: AccessibilityNodeInfo, out: MutableSet<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                walk(child, out)
            } finally {
                child.recycle()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: LauncherAccessibilityService? = null

        private val latestScreenText = AtomicReference("")

        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ).orEmpty()
            val expected = "${context.packageName}/${LauncherAccessibilityService::class.java.name}"
            return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
        }

        fun readLatestText(context: Context): String {
            val service = instance
            return if (service != null) {
                service.captureScreenText()
            } else {
                latestScreenText.get().orEmpty()
            }
        }
    }
}
