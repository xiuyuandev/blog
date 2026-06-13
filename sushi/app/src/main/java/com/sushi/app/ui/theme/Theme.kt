package com.sushi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

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
    outlineVariant = PaperDark,
    error = Cinnabar,
    onError = Paper
)

// 暗色配色：纸→墨黑、墨→纸白、亚麻→深灰、朱砂红保持
private val SushiDarkColorScheme = darkColorScheme(
    primary = Cinnabar,
    onPrimary = Paper,
    secondary = Paper,
    onSecondary = Ink,
    tertiary = BronzeCopper,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = ObsidianBlack,
    onSurfaceVariant = PaperDark,
    outline = InkLight,
    outlineVariant = ObsidianBlack,
    error = Cinnabar,
    onError = Paper
)

// 全局圆角
val CardShape = RoundedCornerShape(10.dp)
val CardShapeSmall = RoundedCornerShape(8.dp)
val CardShapeLarge = RoundedCornerShape(14.dp)
val PillShape = RoundedCornerShape(50.dp)
val DialogShape = RoundedCornerShape(16.dp)

// 全局间距
object SushiSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// 动画时长
object SushiAnim {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
}

@Composable
fun SushiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SushiDarkColorScheme else SushiColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SushiTypography,
        content = content
    )
}
