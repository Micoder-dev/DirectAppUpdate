package com.micoder.directappupdate.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType

/**
 * A root dialog component that wraps the content in a surface with a border and elevation.
 */
@Composable
fun AppDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(
        2.dp,
        MaterialTheme.colorScheme.outline
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current

    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                color = theme.background,
                contentColor = theme.onBackground,
                shape = RoundedCornerShape(LocalSpacing.current.medium),
                border = border,
                tonalElevation = spacing.medium,
                modifier = Modifier
                    .padding(spacing.medium)
                    .fillMaxWidth()
                    .wrapContentSize()
                    .animateContentSize()
                    .then(modifier)
            ) {
                Column(
                    verticalArrangement = verticalArrangement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.medium),
                    content = content
                )
            }
        }
    }
}

/**
 * A dialog that shows the update status and progress.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    dialogState: UpdateDialogState,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {

    val tv = isTelevision()
    AppDialog(
        visible = dialogState.visible,
        onDismiss = onCancelClick,
        modifier = Modifier.widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!tv) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                dialogState.config?.let {
                    Text(text = it.appName, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = dialogState.status, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                if (dialogState.showUpdateButton) {
                    Row {
                        Button(text = "Update App", onClick = onUpdateClick)
                        if (dialogState.updateType == UpdateType.Flexible) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(text = "Cancel", onClick = onCancelClick)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = dialogState.progress / 100,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                dialogState.config?.let {
                    Text(text = it.appName, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = dialogState.status, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                if (dialogState.showUpdateButton) {
                    Row {
                        Button(text = "Update App", onClick = onUpdateClick)
                        if (dialogState.updateType == UpdateType.Flexible) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(text = "Cancel", onClick = onCancelClick)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = dialogState.progress / 100,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

}

@Preview
@Composable
fun UpdateDialogPreview() {
    UpdateDialog(
        dialogState = UpdateDialogState(
            visible = true,
            status = "Downloading update",
            showUpdateButton = false,
            progress = 50f,
            updateType = UpdateType.Flexible
        ),
        onUpdateClick = {},
        onCancelClick = {}
    )
}