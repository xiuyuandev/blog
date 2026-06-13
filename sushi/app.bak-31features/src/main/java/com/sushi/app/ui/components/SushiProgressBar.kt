package com.sushi.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.Linen

/**
 * 统一进度条 - 1层纯色，建立3级层次
 * @param size 卡片 6dp / 详情 8dp / 总览 10dp
 */
@Composable
fun SushiProgressBar(
    progress: Float,
    color: Color,
    trackColor: Color = InkFaintest.copy(alpha = 0.3f),
    size: Dp = 6.dp,
    modifier: Modifier = Modifier
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
