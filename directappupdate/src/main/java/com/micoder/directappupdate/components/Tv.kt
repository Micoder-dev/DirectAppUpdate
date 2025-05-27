package com.micoder.directappupdate.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

val LocalAlwaysTelevision = compositionLocalOf { false }

@Composable
fun isTelevision(): Boolean {
    val alwaysTelevision = LocalAlwaysTelevision.current
    return if (alwaysTelevision) true
    else LocalConfiguration.current.isTelevision()
}

fun Configuration.isTelevision(): Boolean {
    val type = uiMode and Configuration.UI_MODE_TYPE_MASK
    return type == Configuration.UI_MODE_TYPE_TELEVISION
}