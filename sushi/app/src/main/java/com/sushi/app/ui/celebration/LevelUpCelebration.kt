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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushi.app.ui.theme.MaterialColor
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
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialColor.scrim),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 主数字翻牌
                FlipNumber(targetLevel = newLevel, key = newLevel)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "升级",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialColor.onPrimary
                )

                Text(
                    text = skillName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialColor.onPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "从 LV $oldLevel 到 LV $newLevel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.onPrimary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "你向下一段旅程又近了一步。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialColor.onPrimary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 数字翻牌效果 — 0.5 秒从 0 翻到目标值
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
            fontWeight = FontWeight.Black,
            fontSize = 120.sp
        ),
        color = MaterialColor.onPrimary,
        modifier = Modifier.scale(1f)
    )
}

/**
 * Material 3 标准 scrim(半透明遮罩)
 */
private val MaterialColor.scrim: androidx.compose.ui.graphics.Color
    get() = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
