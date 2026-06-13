package com.sushi.app.ui.celebration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.util.HapticType
import com.sushi.app.util.rememberHaptic
import kotlinx.coroutines.delay

/**
 * 升级庆祝动画 - 全屏闪光+印章+震感
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

    val transition = rememberInfiniteTransition(label = "celebration")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val rotation by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 1.2f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cinnabar.copy(alpha = 0.15f))
                .padding(SushiSpacing.xxxl),
            contentAlignment = Alignment.Center
        ) {
            // 印章背景
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .rotate(rotation)
                    .alpha(0.4f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Cinnabar,
                        radius = size.width / 2,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                    drawCircle(
                        color = Paper,
                        radius = size.width / 2 - 12f,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(SushiSpacing.md)
            ) {
                Text(
                    text = "升级",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    ),
                    color = Cinnabar
                )
                Text(
                    text = skillName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(SushiSpacing.sm))
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                ) {
                    Text(
                        text = "LV $oldLevel → LV $newLevel",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (newLevel >= 100) ObsidianBlack else if (newLevel >= 30) AmberGold else if (newLevel >= 10) BronzeCopper else Cinnabar
                    )
                }
                Spacer(modifier = Modifier.height(SushiSpacing.lg))
                Text(
                    text = "你向黑曜之境又近一步",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkLight
                )
            }
        }
    }
}
