package com.personal.tvlauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class LauncherPreferences(private val context: Context) {

    private val hiddenPackagesKey = stringSetPreferencesKey("hidden_packages")
    private val showSystemAppsKey = booleanPreferencesKey("show_system_apps")

    val hiddenPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[hiddenPackagesKey].orEmpty()
    }

    val showSystemApps: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[showSystemAppsKey] ?: false
    }

    suspend fun hidePackage(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[hiddenPackagesKey].orEmpty().toMutableSet()
            current.add(packageName)
            prefs[hiddenPackagesKey] = current
        }
    }

    suspend fun unhidePackage(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[hiddenPackagesKey].orEmpty().toMutableSet()
            current.remove(packageName)
            prefs[hiddenPackagesKey] = current
        }
    }

    suspend fun setShowSystemApps(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[showSystemAppsKey] = show
        }
    }
}
