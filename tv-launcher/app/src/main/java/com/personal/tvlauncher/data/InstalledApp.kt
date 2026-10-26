package com.personal.tvlauncher.data

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    val isSystemApp: Boolean,
)
