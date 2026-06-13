package com.sushi.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.MaterialColor

/**
 * Material 3 标准 ModalBottomSheet 包装
 *
 * 替代旧版 AlertDialog,符合现代 Android 规范。
 *
 * 使用示例:
 * ```
 * SushiBottomSheet(
 *     visible = showSheet,
 *     title = "选择技能",
 *     onDismiss = { showSheet = false }
 * ) {
 *     // 内容
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SushiBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    sheetState: androidx.compose.material3.SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialColor.surface,
        contentColor = MaterialColor.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialColor.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
            content()
        }
    }
}
