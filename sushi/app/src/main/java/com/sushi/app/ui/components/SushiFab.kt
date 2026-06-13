package com.sushi.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 中央 ExtendedFAB · Material 3 规范
 *
 * - elevation 6dp resting / 12dp hover(Material 3 FAB 规范)
 * - shape 默认 16dp(FAB 容器圆角)
 * - containerColor / contentColor 跟随 Material 3 角色
 */
@Composable
fun SushiExtendedFab(
    state: SushiFabState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, text, container) = when (state) {
        SushiFabState.IDLE -> Triple(Icons.Outlined.Add, "开始", MaterialTheme.colorScheme.primaryContainer)
        SushiFabState.RUNNING -> Triple(Icons.Outlined.Pause, "暂停", MaterialTheme.colorScheme.tertiary)
        SushiFabState.PAUSED -> Triple(Icons.Outlined.PlayArrow, "继续", MaterialTheme.colorScheme.primary)
    }
    val onContainer = if (state == SushiFabState.RUNNING || state == SushiFabState.PAUSED) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = container,
        contentColor = onContainer,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(SushiSpacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 圆形 FAB · Material 3 规范
 *
 * - elevation 6dp resting(Material 3 FAB 规范)
 * - shape 16dp 圆角
 */
@Composable
fun SushiFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Outlined.Add,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

enum class SushiFabState {
    IDLE,    // 未开始
    RUNNING, // 计时中
    PAUSED   // 已暂停
}
