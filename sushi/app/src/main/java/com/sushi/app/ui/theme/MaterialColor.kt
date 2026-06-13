package com.sushi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Material 3 颜色 Token 快捷访问。
 *
 * 在 SushiTheme 内部使用时,直接 `MaterialColor.primary` 即可获取当前主题下的色值。
 * 在 SushiTheme 外部使用(Composable 内),也支持。
 */
object MaterialColor {
    val primary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val onPrimary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimary

    val primaryContainer: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primaryContainer

    val onPrimaryContainer: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimaryContainer

    val secondary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondary

    val onSecondary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSecondary

    val secondaryContainer: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondaryContainer

    val tertiary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.tertiary

    val onTertiary: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onTertiary

    val background: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background

    val onBackground: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onBackground

    val surface: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    val onSurface: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val surfaceVariant: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant

    val onSurfaceVariant: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurfaceVariant

    val outline: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline

    val outlineVariant: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outlineVariant

    val error: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error

    val onError: androidx.compose.ui.graphics.Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onError
}
