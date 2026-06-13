package com.sushi.app.ui.celebration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SerifFontFamily
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.util.HapticType
import com.sushi.app.util.rememberHaptic
import kotlinx.coroutines.delay

/**
 * 升级庆祝动画 - **克制版**
 *
 * 替代旧版粒子 + 印章 + 旋转,改为:
 * - 简洁大数字翻牌
 * - 短文案
 * - 3 秒后自动消失
 *
 * Material 3 风格,无华丽动效。
 */
@Composable
fun LevelUpCelebration(
    visible: Boolean,
    oldLevel: Int,
    newLevel: Int,
    skillName: String,
    onDismiss: () -> Unit
) {
    val haptic = rememberHaptic()
    LaunchedEffect(visible) {
        if (visible) {
            haptic(HapticType.CONFIRM)
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(SushiAnim.MEDIUM)),
        exit = fadeOut(animationSpec = tween(SushiAnim.MEDIUM))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialColor.scrim),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(
                        color = MaterialColor.inverseSurface,
                        shape = CardShape
                    )
                    .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
            ) {
                // 主数字翻牌 - Material 3 displayLarge (57sp) 衬线体
                FlipNumber(targetLevel = newLevel, key = newLevel)

                Spacer(modifier = Modifier.height(SushiSpacing.xs))

                Text(
                    text = "升级",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialColor.inverseOnSurface
                )

                Text(
                    text = skillName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialColor.inverseOnSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(SushiSpacing.xs))

                Text(
                    text = "从 LV $oldLevel 到 LV $newLevel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.inverseOnSurface.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(SushiSpacing.sm))

                Text(
                    text = "你向下一段旅程又近了一步。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialColor.inverseOnSurface.copy(alpha = 0.56f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 数字翻牌效果 — 0.8 秒从 0 翻到目标值
 * 使用 Material 3 displayLarge + 衬线体（与时间显示一致）
 */
@Composable
private fun FlipNumber(targetLevel: Int, key: Int) {
    var current by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = current,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "flip"
    )
    LaunchedEffect(key) {
        current = 0f
        current = targetLevel.toFloat()
    }
    Text(
        text = animated.toInt().toString().padStart(2, '0'),
        style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialColor.inversePrimary
    )
}

/** 动画时长 token */
private object SushiAnim {
    const val FAST = 150
    const val MEDIUM = 300
    const val SLOW = 500
}
