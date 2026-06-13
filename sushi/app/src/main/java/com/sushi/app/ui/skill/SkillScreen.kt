package com.sushi.app.ui.skill

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.Profession
import com.sushi.app.data.model.SkillCategory
import com.sushi.app.data.model.TimeRecord
import com.sushi.app.logic.LevelUpEvent
import com.sushi.app.logic.SettlementResult
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.viewmodel.SkillDetail
import com.sushi.app.viewmodel.SkillDisplay
import com.sushi.app.viewmodel.SkillUiState
import com.sushi.app.viewmodel.SkillViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SkillScreen(
    viewModel: SkillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.selectedSkill != null -> {
                SkillDetailContent(
                    detail = uiState.selectedSkill!!,
                    onBack = viewModel::clearSelectedSkill,
                    onAddProfession = { professionId ->
                        viewModel.addProfessionToSkill(
                            uiState.selectedSkill!!.skill.id,
                            professionId
                        )
                    },
                    onRemoveProfession = { professionId ->
                        viewModel.removeProfessionFromSkill(
                            uiState.selectedSkill!!.skill.id,
                            professionId
                        )
                    },
                    onManualInject = viewModel::showManualInject
                )
            }
            else -> {
                SkillListContent(
                    uiState = uiState,
                    onSelectCategory = viewModel::selectCategory,
                    onSelectSkill = viewModel::selectSkill
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Paper),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "…",
                    style = MaterialTheme.typography.headlineMedium,
                    color = InkFaint
                )
            }
        }
    }

    // 手动注入弹窗
    if (uiState.showManualInject && uiState.selectedSkill != null) {
        ManualInjectDialog(
            skillName = uiState.selectedSkill!!.skill.name,
            onConfirm = { minutes, startDt, endDt, desc ->
                viewModel.manualInject(
                    skillId = uiState.selectedSkill!!.skill.id,
                    netDurationMin = minutes,
                    startDateTime = startDt,
                    endDateTime = endDt,
                    description = desc
                )
            },
            onDismiss = viewModel::hideManualInject
        )
    }

    // 手动注入结果
    uiState.manualInjectResult?.let { result ->
        SettlementResultDialog(
            result = result,
            onDismiss = viewModel::dismissManualInjectResult
        )
    }
}

@Composable
private fun SkillListContent(
    uiState: SkillUiState,
    onSelectCategory: (SkillCategory) -> Unit,
    onSelectSkill: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        CategoryTabs(
            selectedCategory = uiState.selectedCategory,
            onSelectCategory = onSelectCategory
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.skills.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无技能",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint
                    )
                }
            } else {
                uiState.skills.forEach { display ->
                    SkillCard(
                        display = display,
                        onClick = { onSelectSkill(display.skill.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: SkillCategory,
    onSelectCategory: (SkillCategory) -> Unit
) {
    val tabs = listOf(
        SkillCategory.COGNITION to "认知",
        SkillCategory.CREATION to "造物",
        SkillCategory.FUNCTION to "功能",
        SkillCategory.STRATEGY to "策略"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEach { (category, label) ->
            val isSelected = category == selectedCategory
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCategory(category) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Ink else InkFaint
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 24.dp else 0.dp)
                        .height(2.dp)
                        .background(if (isSelected) Ink else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SkillCard(
    display: SkillDisplay,
    onClick: () -> Unit
) {
    val tierColor = tierColorFor(display.tierLabel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = display.skill.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
                color = Ink
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${display.level}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
                    color = Ink
                )
                Text(
                    text = display.tierLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = tierColor
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Paper)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(display.progress / 120f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(tierColor.copy(alpha = 0.55f))
                )
            }
            Text(
                text = "${display.progress}/120",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

@Composable
private fun SkillDetailContent(
    detail: SkillDetail,
    onBack: () -> Unit,
    onAddProfession: (String) -> Unit,
    onRemoveProfession: (String) -> Unit,
    onManualInject: () -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        // 返回按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium,
                color = InkLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "返回",
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 等级与材质
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${detail.level}",
                    style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
                    color = Ink
                )
                Text(
                    text = detail.tierLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = tierColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 技能名
            Text(
                text = detail.skill.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
                color = Ink
            )

            // 经验进度条
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "经验值",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Linen)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(detail.progress / 120f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tierColor.copy(alpha = 0.55f))
                        )
                    }
                    Text(
                        text = "${detail.progress}/120",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkFaint
                    )
                }
            }

            // 手动注入按钮
            OutlinedButton(
                onClick = onManualInject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "+ 手动注入时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cinnabar
                )
            }

            // 关联职业
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "关联职业",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    detail.professions.forEach { profession ->
                        ProfessionChip(
                            profession = profession,
                            onRemove = { onRemoveProfession(profession.id) }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Linen)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkFaint
                        )
                    }
                }
            }

            // 词条树
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "词条",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )

                detail.unlockedAffixes.forEach { affix ->
                    AffixItem(affix = affix, isUnlocked = true, tierColor = tierColor)
                }

                detail.lockedAffixes.forEach { affix ->
                    AffixItem(affix = affix, isUnlocked = false, tierColor = tierColor)
                }

                if (detail.unlockedAffixes.isEmpty() && detail.lockedAffixes.isEmpty()) {
                    Text(
                        text = "暂无词条",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                }
            }

            // 历史记录
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )

                if (detail.historyRecords.isEmpty()) {
                    Text(
                        text = "暂无记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                } else {
                    detail.historyRecords.forEach { record ->
                        HistoryRecordItem(record = record)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HistoryRecordItem(record: TimeRecord) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val startText = dateFormat.format(record.startDateTime)
    val endText = dateFormat.format(record.endDateTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${record.netDurationMin}分钟纯时间",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            if (record.isManualEntry) {
                Text(
                    text = "手动",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinnabar
                )
            }
        }
        Text(
            text = "$startText → $endText",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint
        )
        if (record.description.isNotBlank()) {
            Text(
                text = record.description,
                style = MaterialTheme.typography.bodySmall,
                color = InkLight
            )
        }
    }
}

@Composable
private fun ProfessionChip(
    profession: Profession,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Linen)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = profession.name,
            style = MaterialTheme.typography.labelMedium,
            color = Ink
        )
        Text(
            text = "×",
            style = MaterialTheme.typography.labelSmall,
            color = InkFaint,
            modifier = Modifier.clickable(onClick = onRemove)
        )
    }
}

@Composable
private fun AffixItem(
    affix: Affix,
    isUnlocked: Boolean,
    tierColor: Color
) {
    val contentAlpha = if (isUnlocked) 1f else 0.35f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .background(Linen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) tierColor else InkFaint)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = affix.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            Text(
                text = affix.description,
                style = MaterialTheme.typography.bodySmall,
                color = InkLight
            )
        }
        if (!isUnlocked) {
            Text(
                text = "LV${affix.requiredSkillLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

/**
 * 手动注入时间弹窗
 */
@Composable
private fun ManualInjectDialog(
    skillName: String,
    onConfirm: (Int, Long, Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var minutes by remember { mutableIntStateOf(30) }
    var description by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // endDateTime 由 startDateTime + minutes 推导，minutes 变化时自动更新
    var endDateTime by remember { mutableStateOf(startDateTime + 30 * 60 * 1000L) }

    // 当 minutes 变化时同步更新 endDateTime
    LaunchedEffect(minutes, startDateTime) {
        endDateTime = startDateTime + minutes * 60 * 1000L
    }

    val startCal = remember { Calendar.getInstance().apply { timeInMillis = startDateTime } }
    val endCal = remember { Calendar.getInstance().apply { timeInMillis = endDateTime } }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "注入纯时间 → $skillName",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 纯时间分钟数
                Text(
                    text = "纯时间（分钟）",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { minutes = (minutes - 5).coerceAtLeast(1) }
                    ) { Text("-5") }
                    Text(
                        text = "$minutes",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                        color = Ink,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = { minutes += 5 }
                    ) { Text("+5") }
                }

                // 开始时间
                Text(
                    text = "开始时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期选择
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    startCal.set(year, month, day)
                                    startDateTime = startCal.timeInMillis
                                    endDateTime = startDateTime + minutes * 60 * 1000L
                                },
                                startCal.get(Calendar.YEAR),
                                startCal.get(Calendar.MONTH),
                                startCal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dateFormat.format(startDateTime), color = Ink)
                    }
                    // 时间选择
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    startCal.set(Calendar.HOUR_OF_DAY, hour)
                                    startCal.set(Calendar.MINUTE, minute)
                                    startDateTime = startCal.timeInMillis
                                    endDateTime = startDateTime + minutes * 60 * 1000L
                                },
                                startCal.get(Calendar.HOUR_OF_DAY),
                                startCal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(timeFormat.format(startDateTime), color = Ink)
                    }
                }

                // 结束时间
                Text(
                    text = "结束时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    endCal.set(year, month, day)
                                    endDateTime = endCal.timeInMillis
                                },
                                endCal.get(Calendar.YEAR),
                                endCal.get(Calendar.MONTH),
                                endCal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dateFormat.format(endDateTime), color = Ink)
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    endCal.set(Calendar.HOUR_OF_DAY, hour)
                                    endCal.set(Calendar.MINUTE, minute)
                                    endDateTime = endCal.timeInMillis
                                },
                                endCal.get(Calendar.HOUR_OF_DAY),
                                endCal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(timeFormat.format(endDateTime), color = Ink)
                    }
                }

                // 做了什么
                Text(
                    text = "做了什么",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("记录你做了什么…", color = InkFaint) },
                    singleLine = false,
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = InkFaint
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(minutes, startDateTime, endDateTime, description)
                },
                enabled = minutes > 0
            ) {
                Text(text = "确认注入", color = Cinnabar)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = InkLight)
            }
        },
        containerColor = Paper
    )
}

@Composable
private fun SettlementResultDialog(
    result: SettlementResult.Success,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "时间已注入",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (result.levelUpEvents.isNotEmpty()) {
                    result.levelUpEvents.forEach { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = event.skillName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink
                            )
                            Text(
                                text = "LV ${event.oldLevel} → ${event.newLevel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Cinnabar
                            )
                        }
                    }
                }

                if (result.unlockedAffixes.isNotEmpty()) {
                    Text(
                        text = "解锁词条",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkLight
                    )
                    result.unlockedAffixes.forEach { affix ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Cinnabar)
                            )
                            Text(
                                text = affix.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink
                            )
                        }
                    }
                }

                if (result.levelUpEvents.isEmpty() && result.unlockedAffixes.isEmpty()) {
                    Text(
                        text = "纯时间已记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkLight
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "好的", color = Cinnabar)
            }
        },
        containerColor = Paper
    )
}

private fun tierColorFor(tierLabel: String): Color = when (tierLabel) {
    "原石" -> RawStoneGray
    "青铜" -> BronzeCopper
    "赤金" -> AmberGold
    "黑曜石" -> ObsidianBlack
    else -> InkLight
}
