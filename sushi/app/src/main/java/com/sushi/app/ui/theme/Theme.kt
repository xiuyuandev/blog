package com.sushi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * 素时主题配置 · Material 3 规范
 *
 * - 颜色:严格遵循 Material 3 ColorScheme token(primary/onPrimary/primaryContainer/...)
 * - 字号:严格遵循 Material 3 Type Scale(display/headline/title/body/label)
 * - 圆角:严格遵循 Material 3 Shape Scale(4/8/12/16/28)
 * - 间距:4dp 网格
 * - 暗色:所有 token 完整定义,避免运行时 undefined 颜色
 */

// ============ 颜色 Scheme(Material 3 完整 token) ============

private val SushiColorScheme = lightColorScheme(
    // Primary
    primary = Cinnabar,
    onPrimary = Paper,
    primaryContainer = CinnabarFaint,
    onPrimaryContainer = Cinnabar,
    // Secondary
    secondary = Ink,
    onSecondary = Paper,
    secondaryContainer = Linen,
    onSecondaryContainer = Ink,
    // Tertiary
    tertiary = BronzeCopper,
    onTertiary = Paper,
    tertiaryContainer = Linen,
    onTertiaryContainer = BronzeCopper,
    // Error
    error = Cinnabar,
    onError = Paper,
    errorContainer = CinnabarFaint,
    onErrorContainer = Cinnabar,
    // Background / Surface
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = InkLight,
    surfaceTint = Cinnabar,
    // Inverse
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = CinnabarFaint,
    // Outline
    outline = InkFaint,
    outlineVariant = InkFaintest,
    // Scrim
    scrim = ObsidianBlack
)

private val SushiDarkColorScheme = darkColorScheme(
    // Primary
    primary = Cinnabar,
    onPrimary = Paper,
    primaryContainer = Cinnabar,
    onPrimaryContainer = Paper,
    // Secondary
    secondary = Paper,
    onSecondary = Ink,
    secondaryContainer = InkLight,
    onSecondaryContainer = Paper,
    // Tertiary
    tertiary = BronzeCopper,
    onTertiary = Ink,
    tertiaryContainer = BronzeCopper,
    onTertiaryContainer = Ink,
    // Error
    error = Cinnabar,
    onError = Paper,
    errorContainer = Cinnabar,
    onErrorContainer = Paper,
    // Background / Surface
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = ObsidianBlack,
    onSurfaceVariant = PaperDark,
    surfaceTint = Cinnabar,
    // Inverse
    inverseSurface = Paper,
    inverseOnSurface = Ink,
    inversePrimary = Cinnabar,
    // Outline
    outline = PaperDark,
    outlineVariant = ObsidianBlack,
    // Scrim
    scrim = ObsidianBlack
)

// ============ 圆角(Material 3 规范) ============

/** 4dp · 小元素(chip, 小型按钮) */
val ShapeExtraSmall = RoundedCornerShape(4.dp)

/** 8dp · 中等元素 */
val ShapeSmall = RoundedCornerShape(8.dp)

/** 12dp · 卡片(标准 Material 3 Card 推荐值) */
val ShapeMedium = RoundedCornerShape(12.dp)

/** 16dp · 大型卡片、FAB 容器 */
val ShapeLarge = RoundedCornerShape(16.dp)

/** 28dp · Dialog / ModalBottomSheet(Material 3 规范) */
val ShapeExtraLarge = RoundedCornerShape(28.dp)

/** 50% · 圆形(头像/印章) */
val ShapeFull = RoundedCornerShape(50)

/** 兼容旧名 */
val CardShape = ShapeMedium
val CardShapeSmall = ShapeSmall
val CardShapeLarge = ShapeLarge
val PillShape = ShapeFull
val DialogShape = ShapeExtraLarge

// ============ 间距(4dp 网格) ============

object SushiSpacing {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
    val huge = 64.dp
}

// ============ 动画时长 ============

object SushiAnim {
    /** 快速反馈(按下、ripple): 150ms */
    const val FAST = 150

    /** 标准转场(页面、对话框): 300ms */
    const val NORMAL = 300

    /** 慢速(进入、退场): 500ms */
    const val SLOW = 500
}

// ============ 触屏目标 ============

/** Material 3 最小可点击区域 48dp */
val SushiMinTouchTarget = 48.dp

@Composable
fun SushiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SushiDarkColorScheme else SushiColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SushiTypography,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = ShapeExtraSmall,
            small = ShapeSmall,
            medium = ShapeMedium,
            large = ShapeLarge,
            extraLarge = ShapeExtraLarge
        ),
        content = content
    )
}
