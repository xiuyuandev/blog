package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.ui.theme.SilverGray
import com.sushi.app.ui.theme.SushiSpacing

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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items = tags, key = { it }) { t ->
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(CinnabarFaint)
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#$t",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cinnabar
                        )
                        IconButton(
                            onClick = { onTagsChange(tags - t) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Delete,
                                contentDescription = "移除",
                                tint = Cinnabar,
                                modifier = Modifier.size(10.dp)
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it.trim() },
                placeholder = { Text("添加标签", color = InkFaint, style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                shape = PillShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cinnabar,
                    unfocusedBorderColor = InkFaint,
                    cursorColor = Ink,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
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
                    .background(CinnabarFaint)
            ) {
                Icon(
                    imageVector = SushiIcons.Add,
                    contentDescription = "添加",
                    tint = Cinnabar,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 推荐标签
        if (suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items = suggestions, key = { it }) { s ->
                    if (s !in tags) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(Linen)
                                .clickable {
                                    onTagsChange(tags + s)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ $s",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkLight
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
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = priority == value
            val color = when (value) {
                0 -> InkFaint
                1 -> CinnabarLight
                2 -> Cinnabar
                else -> InkFaint
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CardShapeSmall)
                    .background(if (isSelected) color.copy(alpha = 0.15f) else Linen)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) color else Color.Transparent,
                        shape = CardShapeSmall
                    )
                    .clickable { onPriorityChange(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) color else InkLight,
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
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                Text(
                    text = "为什么暂停？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reasons.forEach { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(if (isSelected) CinnabarFaint else Linen)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Cinnabar else Ink
                            )
                        }
                    }
                }
                if (selectedCategory == "OTHER") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("备注", color = InkFaint) },
                        singleLine = true,
                        shape = CardShapeSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cinnabar,
                            unfocusedBorderColor = InkFaint,
                            cursorColor = Ink
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
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.5f)
                )
            ) {
                Text("记录", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后", color = InkLight)
            }
        },
        containerColor = Paper
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
        else -> Cinnabar
    }

    Column(
        modifier = modifier
            .shadow(if (isUnlocked) 1.dp else 0.dp, CardShapeSmall)
            .clip(CardShapeSmall)
            .background(if (isUnlocked) PaperWarm else Linen.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 印章
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) tierColor else InkFaintest.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SushiIcons.Star,
                contentDescription = null,
                tint = if (isUnlocked) Paper else InkFaintest,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isUnlocked) Ink else InkFaint,
            fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = InkFaint,
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
                color = InkFaint
            )
        } else if (isUnlocked) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(tierColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
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
            .background(Linen.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "今日一句",
            style = MaterialTheme.typography.labelSmall,
            color = InkFaint
        )
        Text(
            text = "「$quote」",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Ink
        )
        if (author.isNotBlank()) {
            Text(
                text = "— $author",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

/**
 * 情绪评分选择
 */
@Composable
fun MoodPicker(
    moodScore: Int,
    onMoodChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        1 to "😔",
        2 to "😕",
        3 to "😐",
        4 to "🙂",
        5 to "😊"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { (score, emoji) ->
            val isSelected = moodScore == score
            Box(
                modifier = Modifier
                    .size(if (isSelected) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CinnabarFaint else Linen)
                    .clickable { onMoodChange(score) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
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
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "JSON：完整备份，可重新导入还原全部数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
                Text(
                    text = "CSV：仅时间记录，可在 Excel 中打开分析",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSelectJson,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper
                )
            ) {
                Text("JSON 完整备份")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSelectCsv) {
                    Text("CSV 时间记录", color = InkLight)
                }
            }
        },
        containerColor = Paper
    )
}

/**
 * 同步冲突提示对话框
 */
