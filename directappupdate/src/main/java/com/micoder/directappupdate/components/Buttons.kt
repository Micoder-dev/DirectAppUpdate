@file:Suppress("unused")

package com.micoder.directappupdate.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.Text as TvText

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun Button(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.12f),
    disabledContentColor: Color = containerColor.copy(alpha = 0.38f),
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val tv = isTelevision()
    if (!tv) {
        Button(
            shape = RoundedCornerShape(8.dp),
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor
            )
        ) {
            Text(
                text = text.uppercase()
            )
        }
    } else {
        TvButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .padding(spacing.extraSmall)
                .then(modifier),
            colors = androidx.tv.material3.ButtonDefaults.colors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor
            )
        ) {
            TvText(
                text = text.uppercase()
            )
        }
    }

}