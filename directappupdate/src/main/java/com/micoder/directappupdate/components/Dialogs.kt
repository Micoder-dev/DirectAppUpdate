package com.micoder.directappupdate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.micoder.directappupdate.model.AppUpdateConfig
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType
import kotlinx.coroutines.delay

/**
 * A root dialog component that wraps the content in a surface with a border and elevation.
 */
@Composable
fun GoldenAppDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    val tv = isTelevision()

    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium)
            ) {
                // Glossy background with golden gradient
                Box(
                    modifier = Modifier
                        .then(
                            if (tv) {
                                // Constrain to 50% of screen width for TV
                                Modifier.fillMaxWidth(0.5f)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )
                        .background(
                            brush = GoldenTheme.darkGoldGradient,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(2.dp).align(Alignment.Center)
                ) {
                    Surface(
                        color = GoldenTheme.surface.copy(alpha = 0.95f),
                        contentColor = GoldenTheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(modifier)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.large),
                            content = content
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced update dialog with comprehensive app information display
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    dialogState: UpdateDialogState,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val tv = isTelevision()
    val spacing = LocalSpacing.current

    GoldenAppDialog(
        visible = dialogState.visible,
        onDismiss = onCancelClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        // App Information Section
        dialogState.config?.let { config ->
            // App Name
            if (tv) {
                Text(
                    text = config.appName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldenTheme.secondary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = config.appName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldenTheme.secondary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(spacing.small))

            // Version Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (tv) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "v${getCurrentVersionName(config)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "v${getCurrentVersionName(config)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = GoldenTheme.secondary,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (tv) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "v${config.versionName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = GoldenTheme.secondary
                            )
                        )
                    } else {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "v${config.versionName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = GoldenTheme.secondary
                            )
                        )
                    }
                }
            }

            // Version Code Information
            if (config.versionCode != config.currentVersionCode) {
                Spacer(modifier = Modifier.height(spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (tv) {
                        Text(
                            text = "Build: ${config.currentVersionCode} → ${config.versionCode}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    } else {
                        Text(
                            text = "Build: ${config.currentVersionCode} → ${config.versionCode}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    }
                }
            }

            // Release Notes Section
            if (config.releaseNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(spacing.medium))

                // Release Notes Header
                if (tv) {
                    Text(
                        text = "What's New",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldenTheme.secondary
                        )
                    )
                } else {
                    Text(
                        text = "What's New",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldenTheme.secondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.small))

                // Release Notes Content with scroll support
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (tv) 200.dp else 200.dp)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(spacing.small)
                ) {
                    items(config.releaseNotes.split("#").filter { it.isNotBlank() }) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            if (tv) {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldenTheme.primary
                                    )
                                )
                                Text(
                                    text = note.trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldenTheme.primary
                                    )
                                )
                                Text(
                                    text = note.trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        // Status Section
        if (tv) {
            Text(
                text = dialogState.status,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = dialogState.status,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(spacing.large))

        // Action Section
        if (dialogState.showUpdateButton) {
            if (tv) {
                // TV Button Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterHorizontally)
                ) {
                    val focusRequester = remember { FocusRequester() }
                    GoldenButton(
                        text = if (dialogState.isReadyToInstall) "Install Now" else "Update App",
                        isInstallButton = dialogState.isReadyToInstall,
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f).focusRequester(focusRequester)
                    )

                    LaunchedEffect(Unit) {
                        delay(100)
                        focusRequester.requestFocus()
                    }

                    if (dialogState.updateType == UpdateType.Flexible) {
                        GoldenButton(
                            text = "Later",
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Mobile Button Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    GoldenButton(
                        text = if (dialogState.isReadyToInstall) "Install Now" else "Update App",
                        isInstallButton = dialogState.isReadyToInstall,
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f)
                    )

                    if (dialogState.updateType == UpdateType.Flexible) {
                        GoldenButton(
                            text = "Later",
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            // Progress Section
            Column {
                // Enhanced progress indicator with golden theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(dialogState.progress / 100f)
                            .background(
                                brush = GoldenTheme.goldHorizontalGradient,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.small))

                if (tv) {
                    Text(
                        text = "${dialogState.progress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GoldenTheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "${dialogState.progress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GoldenTheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

}

/**
 * Helper function to get current version name
 */
@Composable
private fun getCurrentVersionName(config: AppUpdateConfig): String {
    val context = LocalContext.current
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun UpdateDialogPreview() {
    UpdateDialog(
        dialogState = UpdateDialogState(
            visible = true,
            status = "Downloading update",
            showUpdateButton = false,
            progress = 50f,
            updateType = UpdateType.Flexible,
            config = AppUpdateConfig(
                appName = "App Name",
                currentVersionCode = 1,
                versionCode = 2,
                versionName = "2.0.0",
                downloadUrl = "",
                apkFileName = "",
                releaseNotes = "",
                immediateUpdate = false
            )
        ),
        onUpdateClick = {},
        onCancelClick = {}
    )
}