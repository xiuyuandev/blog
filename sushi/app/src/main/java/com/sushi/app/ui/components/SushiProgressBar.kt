package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 统一进度条 · Material 3 规范
 *
 * - shape: 圆角(高度/2)
 * - track: surfaceContainerHighest
 * - progress: 自定义色
 * - 三种尺寸:小 4dp / 中 6dp / 大 10dp
 */
@Composable
fun SushiProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 6.dp
) {
    val coerced = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(size)
            .clip(RoundedCornerShape(size / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(coerced)
                .height(size)
                .clip(RoundedCornerShape(size / 2))
                .background(color)
        )
    }
}
