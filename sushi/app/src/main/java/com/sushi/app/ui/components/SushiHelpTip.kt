package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SushiMinTouchTarget

/**
 * 概念解释卡片 - 悬浮提示
 * Material 3 Dialog:28dp 圆角,使用 inverseSurface 容器色
 */
data class ConceptInfo(
    val title: String,
    val description: String
)

@Composable
fun ConceptExplainerCard(
    info: ConceptInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialColor.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialColor.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialColor.onSurface
                )
            }
        },
        text = {
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialColor.primary,
                    contentColor = MaterialColor.onPrimary
                )
            ) {
                Text(
                    text = "明白了",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialColor.surface
    )
}

/**
 * 内联概念提示 - 出现在标签旁的"?"图标
 * Material 3 触屏目标:48dp
 */
@Composable
fun InlineConceptTip(
    info: ConceptInfo,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(20.dp)
            .defaultMinSize(
                minWidth = SushiMinTouchTarget,
                minHeight = SushiMinTouchTarget
            )
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialColor.surfaceVariant)
            .clickable { showDialog = true }
            .semantics { contentDescription = "什么是${info.title}?" },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = SushiIcons.Help,
            contentDescription = null,
            tint = MaterialColor.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }

    if (showDialog) {
        ConceptExplainerCard(info = info, onDismiss = { showDialog = false })
    }
}
