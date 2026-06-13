package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.SushiSpacing

/**
 * 通用空状态
 *
 * Material 3 规范: 居中显示,垂直间距 8dp,Icon 48dp 浅色,主副文案颜色 onSurface/onSurfaceVariant。
 *
 * @param icon 顶部图标(48dp,色 = outline)
 * @param title 主标题(bodyLarge, onSurface)
 * @param subtitle 副标题(bodySmall, onSurfaceVariant)
 * @param actionLabel 行动按钮文案(可选)
 * @param onAction 行动按钮回调(可选)
 */
@Composable
fun SushiEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SushiSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(SushiSpacing.xs))
            Button(
                onClick = onAction,
                shape = CardShapeSmall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * 通用 1px 分隔线 · Material 3 outlineVariant
 */
@Composable
fun SushiDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

/**
 * 标签选择器
 */
@Composable
fun TagPicker(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    suggestions: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var newTag by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
        // 已选标签
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                items(items = tags, key = { it }) { t ->
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(start = 10.dp, end = SushiSpacing.xs, top = SushiSpacing.xs, bottom = SushiSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#$t",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(
                            onClick = { onTagsChange(tags - t) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "移除",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 输入框 + 添加
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it.trim() },
                placeholder = { Text("添加标签", color = MaterialColor.outline, style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                shape = PillShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (newTag.isNotBlank() && newTag !in tags) {
                        onTagsChange(tags + newTag)
                        newTag = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = SushiIcons.Add,
                    contentDescription = "添加",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 推荐标签
        if (suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                items(items = suggestions, key = { it }) { s ->
                    if (s !in tags) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    onTagsChange(tags + s)
                                }
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.xs)
                        ) {
                            Text(
                                text = "+ $s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 优先级选择
 */
@Composable
fun PriorityPicker(
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        0 to "普通",
        1 to "高",
        2 to "紧急"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        options.forEach { (value, label) ->
            val isSelected = priority == value
            val color = when (value) {
                0 -> MaterialTheme.colorScheme.outline
                1 -> MaterialColor.primary
                2 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CardShapeSmall)
                    .background(
                        if (isSelected) color.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onPriorityChange(value) }
                    .padding(vertical = SushiSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 暂停原因选择对话框
 */
@Composable
fun PauseReasonDialog(
    visible: Boolean,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val reasons = listOf(
        "FOOD" to "用餐",
        "PHONE" to "手机",
        "RESTROOM" to "如厕",
        "OTHER" to "其他"
    )
    var customReason by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "暂停原因",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                Text(
                    text = "为什么暂停?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                    reasons.forEach { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (selectedCategory == "OTHER") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("备注", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = CardShapeSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val category = selectedCategory ?: "OTHER"
                    val reason = if (category == "OTHER") customReason else reasons.find { it.first == category }?.second ?: ""
                    onConfirm(reason, category)
                },
                enabled = selectedCategory != null,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialColor.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text("记录", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * 成就徽章
 */
@Composable
fun AchievementBadge(
    name: String,
    description: String,
    tier: String,
    isUnlocked: Boolean,
    progress: Int = 0,
    threshold: Int = 0,
    modifier: Modifier = Modifier
) {
    val tierColor = when (tier) {
        "铜" -> BronzeCopper
        "银" -> SilverGray
        "金" -> AmberGold
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .shadow(if (isUnlocked) 1.dp else 0.dp, CardShapeSmall)
            .clip(CardShapeSmall)
            .background(
                if (isUnlocked) MaterialColor.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(SushiSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        // 印章
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) tierColor else MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SushiIcons.Star,
                contentDescription = null,
                tint = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
        if (!isUnlocked && threshold > 0) {
            SushiProgressBar(
                progress = (progress.toFloat() / threshold).coerceIn(0f, 1f),
                color = tierColor,
                size = 4.dp
            )
            Text(
                text = "$progress/$threshold",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (isUnlocked) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(tierColor.copy(alpha = 0.12f))
                    .padding(horizontal = SushiSpacing.xs, vertical = 1.dp)
            ) {
                Text(
                    text = tier,
                    style = MaterialTheme.typography.labelSmall,
                    color = tierColor
                )
            }
        }
    }
}

/**
 * 每日一句卡片
 */
@Composable
fun DailyQuoteCard(
    quote: String,
    author: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Text(
            text = "今日一句",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "「$quote」",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (author.isNotBlank()) {
            Text(
                text = "— $author",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * CSV 导出对话框
 */
@Composable
fun ExportFormatDialog(
    visible: Boolean,
    onSelectJson: () -> Unit,
    onSelectCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "选择导出格式",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                Text(
                    text = "JSON:完整备份,可重新导入还原全部数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "CSV:仅时间记录,可在 Excel 中打开分析",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSelectJson,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("JSON 完整备份")
            }
        },
        dismissButton = {
            TextButton(onClick = onSelectCsv) {
                Text("CSV 时间记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
