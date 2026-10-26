package com.personal.tvlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

class AppRepository(private val context: Context) {

    fun loadLaunchableApps(): List<InstalledApp> {
        val pm = context.packageManager
        val leanback = query(pm, Intent.CATEGORY_LEANBACK_LAUNCHER)
        val launcher = query(pm, Intent.CATEGORY_LAUNCHER)

        val byComponent = LinkedHashMap<String, ResolveInfo>()
        for (info in leanback + launcher) {
            val key = "${info.activityInfo.packageName}/${info.activityInfo.name}"
            byComponent.putIfAbsent(key, info)
        }

        val selfPackage = context.packageName
        return byComponent.values
            .asSequence()
            .filter { it.activityInfo.packageName != selfPackage }
            .map { info ->
                val appInfo = info.activityInfo.applicationInfo
                InstalledApp(
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    label = info.loadLabel(pm)?.toString().orEmpty()
                        .ifBlank { info.activityInfo.packageName },
                    icon = info.loadIcon(pm),
                    isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launch(app: InstalledApp): Boolean {
        val pm = context.packageManager
        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            if (leanbackIntent.resolveActivity(pm) != null) {
                context.startActivity(leanbackIntent)
                true
            } else {
                val fallback = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(app.packageName, app.activityName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                true
            }
        } catch (_: Exception) {
            val launchIntent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        }
    }

    private fun query(pm: PackageManager, category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        @Suppress("DEPRECATION")
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        } else {
            pm.queryIntentActivities(intent, 0)
        }
    }
}
