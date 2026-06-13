package com.sushi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiMinTouchTarget
import com.sushi.app.ui.theme.SushiSpacing
import kotlinx.coroutines.delay

/**
 * 统一撤销 Snackbar - 4 秒自动消失
 * Material 3 规范:inverseSurface 容器色,触屏目标 48dp
 */
@Composable
fun UndoSnackbar(
    visible: Boolean,
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(4000)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(SushiAnim.NORMAL)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(SushiAnim.NORMAL)
        ) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.md)
                .clip(CardShape)
                .background(MaterialColor.inverseSurface)
                .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.sm)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.inverseOnSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    onUndo()
                    onDismiss()
                },
                modifier = Modifier.defaultMinSize(
                    minWidth = SushiMinTouchTarget,
                    minHeight = SushiMinTouchTarget
                )
            ) {
                Text(
                    text = "撤销",
                    color = MaterialColor.inversePrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
