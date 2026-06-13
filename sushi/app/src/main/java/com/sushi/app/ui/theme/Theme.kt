package com.sushi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SushiColorScheme = lightColorScheme(
    primary = Cinnabar,
    onPrimary = Paper,
    secondary = Ink,
    onSecondary = Paper,
    tertiary = BronzeCopper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = InkLight,
    outline = InkFaint,
    outlineVariant = PaperDark
)

@Composable
fun SushiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SushiColorScheme,
        typography = SushiTypography,
        content = content
    )
}
