package com.personal.tvlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.personal.tvlauncher.ui.HomeScreen
import com.personal.tvlauncher.ui.LauncherViewModel
import kotlinx.coroutines.launch

class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshApps()
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsState()
            HomeScreen(
                state = state,
                onLaunch = viewModel::launchApp,
                onHide = viewModel::hideApp,
                onUnhide = viewModel::unhideApp,
                onToggleSystemApps = viewModel::toggleShowSystemApps,
                onShowHiddenManager = viewModel::setShowHiddenManager,
                onOpenScreenReader = viewModel::openScreenReader,
                onCaptureScreen = viewModel::captureScreenText,
                onCloseScreenReader = viewModel::closeScreenReader,
                onRefresh = viewModel::refreshApps,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
    }
}
