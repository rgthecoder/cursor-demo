package com.personal.tvlauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.personal.tvlauncher.a11y.LauncherAccessibilityService
import com.personal.tvlauncher.data.AppRepository
import com.personal.tvlauncher.data.InstalledApp
import com.personal.tvlauncher.data.LauncherPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<InstalledApp> = emptyList(),
    val allApps: List<InstalledApp> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val showSystemApps: Boolean = false,
    val showHiddenManager: Boolean = false,
    val showScreenReader: Boolean = false,
    val screenText: String = "",
    val accessibilityEnabled: Boolean = false,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val preferences = LauncherPreferences(application)

    private val rawApps = MutableStateFlow(repository.loadLaunchableApps())
    private val showHiddenManager = MutableStateFlow(false)
    private val showScreenReader = MutableStateFlow(false)
    private val screenText = MutableStateFlow("")
    private val accessibilityEnabled = MutableStateFlow(
        LauncherAccessibilityService.isEnabled(application),
    )

    private data class PrefsSlice(
        val hidden: Set<String>,
        val showSystem: Boolean,
    )

    private data class PanelSlice(
        val showHiddenManager: Boolean,
        val showScreenReader: Boolean,
        val screenText: String,
        val accessibilityEnabled: Boolean,
    )

    private val prefsFlow = combine(
        preferences.hiddenPackages,
        preferences.showSystemApps,
    ) { hidden, showSystem ->
        PrefsSlice(hidden, showSystem)
    }

    private val panelsFlow = combine(
        showHiddenManager,
        showScreenReader,
        screenText,
        accessibilityEnabled,
    ) { hiddenMgr, reader, text, a11y ->
        PanelSlice(hiddenMgr, reader, text, a11y)
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        rawApps,
        prefsFlow,
        panelsFlow,
    ) { apps, prefs, panels ->
        val visible = apps.filter { app ->
            app.packageName !in prefs.hidden && (prefs.showSystem || !app.isSystemApp)
        }
        LauncherUiState(
            apps = visible,
            allApps = apps,
            hiddenPackages = prefs.hidden,
            showSystemApps = prefs.showSystem,
            showHiddenManager = panels.showHiddenManager,
            showScreenReader = panels.showScreenReader,
            screenText = panels.screenText,
            accessibilityEnabled = panels.accessibilityEnabled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState(),
    )

    fun refreshApps() {
        rawApps.value = repository.loadLaunchableApps()
        accessibilityEnabled.value =
            LauncherAccessibilityService.isEnabled(getApplication())
    }

    fun launchApp(app: InstalledApp) {
        repository.launch(app)
    }

    fun hideApp(packageName: String) {
        viewModelScope.launch { preferences.hidePackage(packageName) }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch { preferences.unhidePackage(packageName) }
    }

    fun setShowSystemApps(show: Boolean) {
        viewModelScope.launch { preferences.setShowSystemApps(show) }
    }

    fun toggleShowSystemApps() {
        viewModelScope.launch { preferences.toggleShowSystemApps() }
    }

    fun setShowHiddenManager(show: Boolean) {
        showHiddenManager.value = show
    }

    fun openScreenReader() {
        accessibilityEnabled.value =
            LauncherAccessibilityService.isEnabled(getApplication())
        screenText.value = LauncherAccessibilityService.readLatestText(getApplication())
        showScreenReader.value = true
    }

    fun captureScreenText() {
        accessibilityEnabled.value =
            LauncherAccessibilityService.isEnabled(getApplication())
        screenText.update {
            LauncherAccessibilityService.readLatestText(getApplication())
        }
    }

    fun closeScreenReader() {
        showScreenReader.value = false
    }
}
