package com.micoder.directappupdate.dialogs

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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType

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
                shape = RoundedCornerShape(8.dp),
                border = border,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentSize()
                    .animateContentSize()
                    .then(modifier)
            ) {
                Column(
                    verticalArrangement = verticalArrangement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun UpdateDialog(
    dialogState: UpdateDialogState,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {

    AppDialog(
        visible = dialogState.visible,
        onDismiss = onCancelClick,
        modifier = Modifier.widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = dialogState.status)
            if (dialogState.showUpdateButton) {
                Row {
                    Button(onClick = onUpdateClick) {
                        Text(text = "Update App")
                    }
                    if (dialogState.updateType == UpdateType.Flexible) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onCancelClick) {
                            Text(text = "Cancel")
                        }
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