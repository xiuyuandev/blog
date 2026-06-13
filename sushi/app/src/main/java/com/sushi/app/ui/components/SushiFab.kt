package com.sushi.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
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
import com.sushi.app.ui.theme.MaterialColor

/**
 * 中央 ExtendedFAB(开始/暂停/继续)
 *
 * 替代旧版圆形 FAB,符合 Material 3 规范。
 *
 * - [state] = Idle: 显示 "+ 开始",绿色 primaryContainer
 * - [state] = Running: 显示 "暂停",secondary container
 * - [state] = Paused: 显示 "继续",primary container
 */
@Composable
fun SushiExtendedFab(
    state: SushiFabState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, text, container) = when (state) {
        SushiFabState.IDLE -> Triple(Icons.Outlined.Add, "开始", MaterialColor.primaryContainer)
        SushiFabState.RUNNING -> Triple(Icons.Outlined.Pause, "暂停", MaterialColor.tertiary)
        SushiFabState.PAUSED -> Triple(Icons.Outlined.PlayArrow, "继续", MaterialColor.primary)
    }
    val onContainer = if (state == SushiFabState.RUNNING || state == SushiFabState.PAUSED) {
        MaterialColor.onPrimary
    } else {
        MaterialColor.onPrimaryContainer
    }

    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = container,
        contentColor = onContainer,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 旧版圆形 FAB(用于非主操作)
 *
 * 保留兼容,供次要操作使用。
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
        containerColor = MaterialColor.primary,
        contentColor = MaterialColor.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
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
