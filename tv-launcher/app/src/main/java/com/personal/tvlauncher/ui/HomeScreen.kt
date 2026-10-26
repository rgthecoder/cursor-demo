package com.personal.tvlauncher.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.personal.tvlauncher.R
import com.personal.tvlauncher.data.InstalledApp

private val ScreenBg = Color(0xFF0B1220)
private val PanelBg = Color(0xFF152033)
private val Accent = Color(0xFF42A5F5)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    state: LauncherUiState,
    onLaunch: (InstalledApp) -> Unit,
    onHide: (String) -> Unit,
    onUnhide: (String) -> Unit,
    onToggleSystemApps: (Boolean) -> Unit,
    onShowHiddenManager: (Boolean) -> Unit,
    onOpenScreenReader: () -> Unit,
    onCaptureScreen: () -> Unit,
    onCloseScreenReader: () -> Unit,
    onRefresh: () -> Unit,
) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(horizontal = 48.dp, vertical = 28.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolbarButton(stringResource(R.string.refresh), onRefresh)
                        ToolbarButton(stringResource(R.string.read_screen), onOpenScreenReader)
                        ToolbarButton(stringResource(R.string.manage_hidden)) {
                            onShowHiddenManager(true)
                        }
                        ToolbarButton(
                            label = if (state.showSystemApps) {
                                "${stringResource(R.string.show_system_apps)}: ON"
                            } else {
                                "${stringResource(R.string.show_system_apps)}: OFF"
                            },
                        ) {
                            onToggleSystemApps(!state.showSystemApps)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.apps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                        AppTile(
                            app = app,
                            onClick = { onLaunch(app) },
                            onLongHide = { onHide(app.packageName) },
                        )
                    }
                }
            }

            if (state.showHiddenManager) {
                HiddenAppsPanel(
                    allApps = state.allApps,
                    hidden = state.hiddenPackages,
                    onUnhide = onUnhide,
                    onHide = onHide,
                    onClose = { onShowHiddenManager(false) },
                )
            }

            if (state.showScreenReader) {
                ScreenReaderPanel(
                    accessibilityEnabled = state.accessibilityEnabled,
                    screenText = state.screenText,
                    onCapture = onCaptureScreen,
                    onClose = onCloseScreenReader,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ToolbarButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = Accent, elevation = 4.dp),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = PanelBg,
            focusedContainerColor = Accent.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppTile(
    app: InstalledApp,
    onClick: () -> Unit,
    onLongHide: () -> Unit,
) {
    Surface(
        onClick = onClick,
        onLongClick = onLongHide,
        modifier = Modifier
            .width(140.dp)
            .height(140.dp),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = Accent, elevation = 8.dp),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = PanelBg,
            focusedContainerColor = Color(0xFF1E3A5F),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AppIcon(drawable = app.icon, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(10.dp))
            Text(
                text = app.label,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) {
        drawable.toBitmap(width = 128, height = 128).asImageBitmap()
    }
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HiddenAppsPanel(
    allApps: List<InstalledApp>,
    hidden: Set<String>,
    onUnhide: (String) -> Unit,
    onHide: (String) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(420.dp),
            colors = ClickableSurfaceDefaults.colors(containerColor = PanelBg),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.manage_hidden),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    ToolbarButton(stringResource(R.string.close), onClose)
                }
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    allApps.distinctBy { it.packageName }.forEach { app ->
                        val isHidden = app.packageName in hidden
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = app.label,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            ToolbarButton(
                                if (isHidden) {
                                    stringResource(R.string.unhide_app)
                                } else {
                                    stringResource(R.string.hide_app)
                                },
                            ) {
                                if (isHidden) onUnhide(app.packageName) else onHide(app.packageName)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ScreenReaderPanel(
    accessibilityEnabled: Boolean,
    screenText: String,
    onCapture: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(440.dp),
            colors = ClickableSurfaceDefaults.colors(containerColor = PanelBg),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.read_screen),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!accessibilityEnabled) {
                            ToolbarButton(stringResource(R.string.enable_accessibility)) {
                                context.startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                            }
                        }
                        ToolbarButton(stringResource(R.string.refresh), onCapture)
                        ToolbarButton(stringResource(R.string.close), onClose)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val body = when {
                    !accessibilityEnabled -> stringResource(R.string.a11y_disabled_hint)
                    screenText.isBlank() -> stringResource(R.string.no_screen_text)
                    else -> screenText
                }
                Text(
                    text = body,
                    color = Color(0xFFECEFF1),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}
