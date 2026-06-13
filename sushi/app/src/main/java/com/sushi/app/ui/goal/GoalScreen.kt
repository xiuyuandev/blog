package com.sushi.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sushi.app.data.model.Goal
import com.sushi.app.data.model.Skill
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiFab
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SushiMinTouchTarget
import com.sushi.app.ui.theme.SushiSpacing
import java.util.Calendar

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialColor.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SushiBackButton(onClick = onBack)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SushiSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
            ) {
                Text(text = "目标", style = MaterialTheme.typography.headlineMedium, color = MaterialColor.onSurface)

                if (uiState.goals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "尚未设定目标", style = MaterialTheme.typography.bodyLarge, color = MaterialColor.outline)
                            Text(text = "点击 + 设定本周或本月目标", style = MaterialTheme.typography.bodySmall, color = MaterialColor.outline)
                        }
                    }
                }

                uiState.goals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        onDeactivate = { viewModel.deactivateGoal(goal.id) },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        SushiFab(
            onClick = viewModel::showCreateDialog,
            icon = SushiIcons.Add,
            contentDescription = "新目标"
        )

        if (uiState.showCreateDialog) {
            CreateGoalDialog(
                allSkills = uiState.allSkills,
                onDismiss = viewModel::hideCreateDialog,
                onCreate = viewModel::createGoal
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val percent = if (goal.targetMinutes > 0) (goal.currentMinutes.toFloat() / goal.targetMinutes).coerceIn(0f, 1f) else 0f
    val periodLabel = when (goal.period) {
        "WEEKLY" -> "本周"
        "MONTHLY" -> "本月"
        else -> "自定义"
    }
    val scope = goal.scopeSkillId?.let { id -> uiStateSkillName(id, goal) } ?: "全部技能"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surfaceVariant)
            .padding(SushiSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialColor.onSurface
                )
                Text(
                    text = "$periodLabel · $scope",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialColor.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(SushiMinTouchTarget)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .clickable { showMenu = true }
                    .background(MaterialColor.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SushiIcons.More,
                    contentDescription = "更多",
                    tint = MaterialColor.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "停用",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onDeactivate()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "删除",
                                color = MaterialColor.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${goal.currentMinutes} / ${goal.targetMinutes} 分",
                style = MaterialTheme.typography.titleMedium,
                color = if (goal.currentMinutes >= goal.targetMinutes) MaterialColor.primary else MaterialColor.onSurface
            )
            Text(
                text = "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialColor.primary
            )
        }

        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialColor.outlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (goal.currentMinutes >= goal.targetMinutes) MaterialColor.primary else MaterialColor.primary)
            )
        }
    }
}

@Composable
private fun CreateGoalDialog(
    allSkills: List<Skill>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var period by rememberSaveable { mutableStateOf("WEEKLY") }
    var targetMinutes by rememberSaveable { mutableStateOf("") }
    var selectedSkillId by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = { Text("新目标", style = MaterialTheme.typography.headlineSmall, color = MaterialColor.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("目标名称", color = MaterialColor.onSurfaceVariant) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialColor.primary,
                        unfocusedBorderColor = MaterialColor.outline,
                        cursorColor = MaterialColor.onSurface,
                        focusedTextColor = MaterialColor.onSurface,
                        unfocusedTextColor = MaterialColor.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 周期选择 - Material 3 触屏目标 48dp
                Row(horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    listOf("WEEKLY" to "周", "MONTHLY" to "月", "CUSTOM" to "自定义").forEach { (value, label) ->
                        val isSelected = period == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = SushiMinTouchTarget)
                                .clip(CardShapeSmall)
                                .background(if (isSelected) MaterialColor.primaryContainer else MaterialColor.surfaceVariant)
                                .clickable { period = value },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialColor.primary else MaterialColor.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetMinutes,
                    onValueChange = { targetMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("目标分钟数", color = MaterialColor.onSurfaceVariant) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialColor.primary,
                        unfocusedBorderColor = MaterialColor.outline,
                        cursorColor = MaterialColor.onSurface,
                        focusedTextColor = MaterialColor.onSurface,
                        unfocusedTextColor = MaterialColor.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 技能范围 - Material 3 触屏目标 48dp
                Text(
                    text = "限定技能（可选）",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialColor.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = SushiMinTouchTarget)
                        .clip(CardShapeSmall)
                        .background(if (selectedSkillId == null) MaterialColor.primaryContainer else MaterialColor.surfaceVariant)
                        .clickable { selectedSkillId = null }
                        .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "全部技能",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedSkillId == null) MaterialColor.primary else MaterialColor.onSurfaceVariant
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                    allSkills.forEach { skill ->
                        val isSelected = selectedSkillId == skill.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = SushiMinTouchTarget)
                                .clip(CardShapeSmall)
                                .background(if (isSelected) MaterialColor.primaryContainer else MaterialColor.surfaceVariant)
                                .clickable { selectedSkillId = skill.id }
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialColor.primary else MaterialColor.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = targetMinutes.toIntOrNull() ?: 0
                    if (name.isNotBlank() && mins > 0) {
                        onCreate(name.trim(), period, mins, selectedSkillId)
                    }
                },
                enabled = name.isNotBlank() && (targetMinutes.toIntOrNull() ?: 0) > 0,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialColor.primary,
                    contentColor = MaterialColor.onPrimary,
                    disabledContainerColor = MaterialColor.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialColor.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "创建",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(
                    minWidth = SushiMinTouchTarget,
                    minHeight = SushiMinTouchTarget
                )
            ) {
                Text(
                    text = "取消",
                    color = MaterialColor.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialColor.surface
    )
}

@Composable
private fun uiStateSkillName(skillId: String, goal: Goal): String = goal.name

private fun currentPeriodRangeMillis(period: String): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    val endCal = Calendar.getInstance()
    when (period) {
        "WEEKLY" -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            endCal.timeInMillis = cal.timeInMillis
            endCal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        "MONTHLY" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            endCal.timeInMillis = cal.timeInMillis
            endCal.add(Calendar.MONTH, 1)
        }
        else -> {
            endCal.add(Calendar.MONTH, 1)
        }
    }
    return cal.timeInMillis to endCal.timeInMillis
}
