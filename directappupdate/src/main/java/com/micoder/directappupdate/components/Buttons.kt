@file:Suppress("unused")

package com.micoder.directappupdate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardBorder
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CardScale
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.micoder.directappupdate.components.GoldenTheme.bluishWhiteTextColor
import com.micoder.directappupdate.components.GoldenTheme.goldLayerTextColor
import com.micoder.directappupdate.components.GoldenTheme.horizontalGoldGradient
import com.micoder.directappupdate.components.GoldenTheme.horizontalLiveGoldGradient

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GoldenButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isInstallButton: Boolean = false,
    onClick: () -> Unit
) {
    val tv = isTelevision()

    if (!tv) {
        Box(
            modifier = modifier
                .background(
                    brush = if (enabled) GoldenTheme.goldHorizontalGradient2 else Brush.horizontalGradient(
                        listOf(Color.Gray, Color.DarkGray)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = if (enabled) Color.Black else Color.Gray,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )
        }
    } else {
        TvIconButton(
            modifier = modifier,
            text = text.uppercase(),
            onClick = { if (enabled) { onClick() } },
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            border = CardDefaults.border(
                border = Border.None,
                focusedBorder = Border(border = BorderStroke(1.dp, horizontalLiveGoldGradient)),
                pressedBorder = Border.None
            ),
            scale = CardDefaults.scale(focusedScale = 1.05f),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvIconButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.padding(top = 20.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(8.dp),
    border: CardBorder = CardDefaults.border(
        border = Border.None,
        focusedBorder = Border.None,
        pressedBorder = Border.None
    ),
    focusedGradient: Brush = horizontalGoldGradient,
    unFocusedGradient: Brush = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ),
    iconContentDescription: String = "Play",
    focusedIconTint: Color = goldLayerTextColor,
    unfocusedIconTint: Color = bluishWhiteTextColor,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    textColorFocused: Color = goldLayerTextColor,
    textColorUnfocused: Color = bluishWhiteTextColor,
    scale: CardScale = CardDefaults.scale(),
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    Card(
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        shape = CardDefaults.shape(shape),
        border = border,
        scale = scale
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(if (isFocused) focusedGradient else unFocusedGradient),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    color = if (isFocused) textColorFocused else textColorUnfocused,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}