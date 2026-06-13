package com.sushi.app.ui.focus

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushi.app.data.model.Skill
import com.sushi.app.data.model.Task
import com.sushi.app.logic.LevelUpEvent
import com.sushi.app.logic.SettlementResult
import com.sushi.app.ui.celebration.LevelUpCelebration
import com.sushi.app.ui.components.PauseReasonDialog
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.SerifFontFamily
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.util.HapticType
import com.sushi.app.util.rememberHaptic
import com.sushi.app.viewmodel.FocusViewModel
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHaptic()

    // 暂停原因对话框（点击暂停后弹出）
    PauseReasonDialog(
        visible = uiState.showPauseReasonDialog,
        onConfirm = { reason, category ->
            haptic(HapticType.KEYBOARD_TAP)
            viewModel.confirmPause(reason, category)
        },
        onDismiss = { viewModel.dismissPauseReasonDialog() }
    )

    // 毕业提示对话框
    if (uiState.showGraduationHint) {
        GraduationHintDialog(
            onConfirm = { message ->
                haptic(HapticType.LONG_PRESS)
                viewModel.graduateSkill(message)
            },
            onDismiss = { viewModel.dismissGraduationHint() }
        )
    }

    // 升级庆祝（升级时全屏动效）- 在结算对话框显示后弹出
    val levelUpEvent = uiState.settlementResult?.levelUpEvents?.firstOrNull()
    if (levelUpEvent != null && !uiState.showGraduationHint) {
        LevelUpCelebration(
            visible = true,
            oldLevel = levelUpEvent.oldLevel,
            newLevel = levelUpEvent.newLevel,
            skillName = levelUpEvent.skillName,
            onDismiss = { viewModel.dismissSettlementResult() }
        )
    }

    when {
        uiState.settlementResult != null -> {
            SettlementResultDialog(
                result = uiState.settlementResult!!,
                taskCompleted = uiState.settlementResult != null && uiState.currentTaskId != null,
                onDismiss = {
                    haptic(HapticType.CONFIRM)
                    viewModel.dismissSettlementResult()
                }
            )
        }
        uiState.showSettlement -> {
            SettlementDialog(
                rawDurationMin = uiState.rawDurationMin,
                netDurationMin = uiState.netDurationMin,
                interruptCount = uiState.interruptCount,
                lastPauseReason = uiState.lastPauseReason,
                description = uiState.description,
                onNetDurationChange = viewModel::setNetDuration,
                onAdjustNetDuration = viewModel::adjustNetDuration,
                onDescriptionChange = viewModel::updateDescription,
                onConfirm = {
                    haptic(HapticType.CONFIRM)
                    viewModel.confirmSettlement()
                },
                onCancel = {
                    haptic(HapticType.REJECT)
                    viewModel.dismissSettlement()
                }
            )
        }
        else -> {
            Crossfade(
                targetState = uiState.isFocusing,
                animationSpec = tween(durationMillis = 400),
                label = "focusStateTransition"
            ) { isFocusing ->
                if (isFocusing) {
                    FocusTimerContent(
                        taskId = uiState.currentTaskId ?: uiState.currentSkillId ?: "",
                        taskName = uiState.currentTaskName ?: "",
                        elapsedSeconds = uiState.elapsedSeconds,
                        isPaused = uiState.isPaused,
                        interruptCount = uiState.interruptCount,
                        lastPauseReason = uiState.lastPauseReason,
                        isFullScreen = uiState.isFullScreen,
                        onTick = viewModel::updateElapsedTime,
                        onPause = {
                            haptic(HapticType.KEYBOARD_TAP)
                            viewModel.pauseFocus()
                        },
                        onResume = {
                            haptic(HapticType.KEYBOARD_TAP)
                            viewModel.resumeFocus()
                        },
                        onStop = {
                            haptic(HapticType.REJECT)
                            viewModel.stopFocus()
                        },
                        onToggleFullScreen = {
                            haptic(HapticType.KEYBOARD_TAP)
                            viewModel.toggleFullScreen()
                        }
                    )
                } else {
                    TaskSelectionContent(
                        activeTasks = uiState.activeTasks,
                        completedTasks = uiState.completedTasks,
                        allSkills = uiState.allSkills,
                        isCreatingTask = uiState.isCreatingTask,
                        onSelectTask = { task, skillId ->
                            haptic(HapticType.CONFIRM)
                            viewModel.startFocus(task.id, task.name, skillId)
                        },
                        onStartFocusBySkill = { skillId, skillName ->
                            haptic(HapticType.CONFIRM)
                            viewModel.startFocusBySkill(skillId, skillName)
                        },
                        onShowCreateTask = {
                            haptic(HapticType.KEYBOARD_TAP)
                            viewModel.showCreateTask()
                        },
                        onHideCreateTask = viewModel::hideCreateTask,
                        onCreateTask = viewModel::createTask,
                        onReactivateTask = viewModel::reactivateTask,
                        onDeleteTask = viewModel::deleteTask
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskSelectionContent(
    activeTasks: List<Task>,
    completedTasks: List<Task>,
    allSkills: List<Skill>,
    isCreatingTask: Boolean,
    onSelectTask: (Task, String) -> Unit,
    onStartFocusBySkill: (String, String) -> Unit,
    onShowCreateTask: () -> Unit,
    onHideCreateTask: () -> Unit,
    onCreateTask: (name: String, skillId: String, priority: Int, isTemplate: Boolean, estimatedMin: Int?) -> Unit,
    onReactivateTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0=任务, 1=技能, 2=已完成

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialColor.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SushiSpacing.xxl, vertical = SushiSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
        ) {
            Text(
                text = "选择任务",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialColor.onSurface
            )

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            // Tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialColor.surface),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                TabItem(
                    text = "任务",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "技能",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "已完成",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            when (selectedTab) {
                0 -> {
                    if (activeTasks.isEmpty()) {
                        EmptyTabState(
                            icon = SushiIcons.Play,
                            title = "暂无进行中的任务",
                            subtitle = "点击下方按钮创建一个新任务吧"
                        )
                    } else {
                        activeTasks.forEach { task ->
                            val skillName = allSkills.find { it.id == task.linkedSkillId }?.name ?: ""
                            TaskCard(
                                task = task,
                                skillName = skillName,
                                onClick = { onSelectTask(task, task.linkedSkillId) }
                            )
                        }
                    }
                }
                1 -> {
                    if (allSkills.isEmpty()) {
                        EmptyTabState(
                            icon = SushiIcons.Inventory,
                            title = "暂无技能",
                            subtitle = "请先在技能页创建技能"
                        )
                    } else {
                        allSkills.forEach { skill ->
                            SkillCard(
                                skill = skill,
                                onClick = { onStartFocusBySkill(skill.id, skill.name) }
                            )
                        }
                    }
                }
                2 -> {
                    if (completedTasks.isEmpty()) {
                        EmptyTabState(
                            icon = SushiIcons.Check,
                            title = "暂无已完成的任务",
                            subtitle = "完成任务后会出现在这里"
                        )
                    } else {
                        completedTasks.forEach { task ->
                            val skillName = allSkills.find { it.id == task.linkedSkillId }?.name ?: ""
                            CompletedTaskCard(
                                task = task,
                                skillName = skillName,
                                onReactivate = { onReactivateTask(task.id) },
                                onDelete = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }

        // Only show "新任务" button when on 任务 tab
        if (selectedTab == 0) {
            Button(
                onClick = onShowCreateTask,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SushiSpacing.xxxl)
                    .shadow(2.dp, CardShape),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialColor.primary,
                    contentColor = MaterialColor.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = SushiIcons.Add,
                        contentDescription = null,
                        tint = MaterialColor.surface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "新任务",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (isCreatingTask) {
        CreateTaskDialog(
            allSkills = allSkills,
            onDismiss = onHideCreateTask,
            onCreate = onCreateTask
        )
    }
}

@Composable
private fun EmptyTabState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialColor.outline,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialColor.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialColor.outline
            )
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CardShapeSmall)
            .background(if (selected) MaterialColor.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = SushiSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialColor.surface else MaterialColor.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    skillName: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialColor.onSurface
        )
        if (skillName.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = SushiIcons.Inventory,
                    contentDescription = null,
                    tint = MaterialColor.primary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = skillName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialColor.primary
                )
            }
        }
    }
}

@Composable
private fun SkillCard(
    skill: Skill,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Text(
            text = skill.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialColor.onSurface
        )
        Text(
            text = skillCategoryLabel(skill.category),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialColor.primary
        )
    }
}

private fun skillCategoryLabel(category: com.sushi.app.data.model.SkillCategory): String =
    when (category) {
        com.sushi.app.data.model.SkillCategory.COGNITION -> "认知"
        com.sushi.app.data.model.SkillCategory.CREATION -> "造物"
        com.sushi.app.data.model.SkillCategory.FUNCTION -> "功能"
        com.sushi.app.data.model.SkillCategory.STRATEGY -> "策略"
    }

@Composable
private fun CompletedTaskCard(
    task: Task,
    skillName: String,
    onReactivate: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surface)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            Icon(
                imageVector = SushiIcons.Check,
                contentDescription = null,
                tint = MaterialColor.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialColor.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = SushiIcons.Delete,
                    contentDescription = "删除",
                    tint = MaterialColor.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (skillName.isNotBlank()) {
            Text(
                text = skillName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialColor.primaryContainer
            )
        }
        Spacer(modifier = Modifier.height(SushiSpacing.xs))
        OutlinedButton(
            onClick = onReactivate,
            shape = CardShapeSmall,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialColor.surfaceVariant,
                contentColor = MaterialColor.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = SushiIcons.Sync,
                    contentDescription = null,
                    tint = MaterialColor.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Text(text = "重新激活")
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    allSkills: List<Skill>,
    onDismiss: () -> Unit,
    onCreate: (name: String, skillId: String, priority: Int, isTemplate: Boolean, estimatedMin: Int?) -> Unit
) {
    var taskName by rememberSaveable { mutableStateOf("") }
    var selectedSkillId by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableIntStateOf(0) }
    var isTemplate by rememberSaveable { mutableStateOf(false) }
    var estimatedMinText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "新建任务",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialColor.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("任务名称", color = MaterialColor.outline) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialColor.surfaceVariant,
                        unfocusedContainerColor = MaterialColor.surfaceVariant,
                        cursorColor = MaterialColor.onSurface,
                        focusedIndicatorColor = MaterialColor.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // #15 估算时长
                TextField(
                    value = estimatedMinText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            estimatedMinText = newValue
                        }
                    },
                    placeholder = { Text("估算时长（分钟，可选）", color = MaterialColor.outline) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialColor.surfaceVariant,
                        unfocusedContainerColor = MaterialColor.surfaceVariant,
                        cursorColor = MaterialColor.onSurface,
                        focusedIndicatorColor = MaterialColor.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // #15 优先级
                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialColor.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to "普通", 1 to "重要", 2 to "紧急").forEach { (level, label) ->
                            val isSelected = priority == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(PillShape)
                                    .background(if (isSelected) MaterialColor.primary else MaterialColor.surfaceVariant)
                                    .clickable { priority = level }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialColor.surface else MaterialColor.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // #17 是否为模板
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShapeSmall)
                        .clickable { isTemplate = !isTemplate }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CardShapeSmall)
                            .background(if (isTemplate) MaterialColor.primary else MaterialColor.outlineVariant)
                    )
                    Text(
                        text = "保存为模板（可重复使用）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialColor.onSurface
                    )
                }

                Text(
                    text = "绑定技能",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialColor.onSurfaceVariant
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
                ) {
                    allSkills.forEach { skill ->
                        val isSelected = selectedSkillId == skill.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(if (isSelected) MaterialColor.primaryContainer else MaterialColor.surfaceVariant)
                                .clickable { selectedSkillId = skill.id }
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialColor.primary else MaterialColor.outline)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = SushiIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialColor.surface,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SushiSpacing.sm))
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialColor.onSurface else MaterialColor.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank() && selectedSkillId.isNotBlank()) {
                        val estMin = estimatedMinText.toIntOrNull()
                        onCreate(
                            taskName.trim(),
                            selectedSkillId,
                            priority,
                            isTemplate,
                            estMin
                        )
                    }
                },
                enabled = taskName.isNotBlank() && selectedSkillId.isNotBlank(),
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
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = MaterialColor.onSurfaceVariant)
            }
        },
        containerColor = MaterialColor.surface
    )
}

@Composable
private fun FocusTimerContent(
    taskId: String,
    taskName: String,
    elapsedSeconds: Int,
    isPaused: Boolean,
    interruptCount: Int = 0,
    lastPauseReason: String? = null,
    isFullScreen: Boolean = false,
    onTick: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onToggleFullScreen: () -> Unit = {}
) {
    val isPausedState by rememberUpdatedState(isPaused)
    LaunchedEffect(taskId) {
        var seconds = elapsedSeconds
        while (true) {
            delay(1000)
            if (!isPausedState) {
                seconds++
                onTick(seconds)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )

    val progressAngle = ((elapsedSeconds % 3600) / 3600f) * 360f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialColor.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) MaterialColor.outline else MaterialColor.primary)
                )
                Text(
                    text = if (isPaused) "已暂停" else "专注中",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialColor.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = taskName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 3.dp.toPx()
                    val arcPadding = strokeWidth / 2
                    drawArc(
                        color = MaterialColor.surfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = MaterialColor.primary,
                        startAngle = -90f,
                        sweepAngle = progressAngle,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = formatElapsedTime(elapsedSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        lineHeight = 64.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SerifFontFamily,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialColor.onSurface
                )
            }

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialColor.primary.copy(alpha = 0.15f))
                        .alpha(if (isPaused) 0.3f else breathingAlpha)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialColor.primary)
                        .alpha(if (isPaused) 0.3f else breathingAlpha)
                )
            }

            // #11 中断次数 + #12 暂停原因 显示
            if (interruptCount > 0) {
                Spacer(modifier = Modifier.height(SushiSpacing.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CardShapeSmall)
                            .background(MaterialColor.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "已暂停 $interruptCount 次",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialColor.primary
                        )
                    }
                    if (!lastPauseReason.isNullOrBlank()) {
                        Text(
                            text = "· $lastPauseReason",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialColor.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SushiSpacing.lg))

            // #24 全屏模式提示
            if (isFullScreen) {
                Box(
                    modifier = Modifier
                        .clip(CardShapeSmall)
                        .background(MaterialColor.onSurface)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "全屏专注中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialColor.surface
                    )
                }
                Spacer(modifier = Modifier.height(SushiSpacing.sm))
            }

            // Pause/Resume + Stop buttons with icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md)
            ) {
                if (isPaused) {
                    Button(
                        onClick = onResume,
                        shape = CardShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialColor.primary,
                            contentColor = MaterialColor.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Play,
                                contentDescription = null,
                                tint = MaterialColor.surface,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(text = "继续")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onPause,
                        shape = CardShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialColor.surface,
                            contentColor = MaterialColor.onSurfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = 12.dp)
                                    .background(MaterialColor.onSurfaceVariant)
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = 12.dp)
                                    .background(MaterialColor.onSurfaceVariant)
                            )
                            Text(text = "暂停")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onStop,
                    shape = CardShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialColor.surface,
                        contentColor = MaterialColor.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(MaterialColor.primary)
                        )
                        Text(text = "停止")
                    }
                }

                // #24 全屏切换
                OutlinedButton(
                    onClick = onToggleFullScreen,
                    shape = CardShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFullScreen) MaterialColor.onSurface else MaterialColor.surface,
                        contentColor = if (isFullScreen) MaterialColor.surface else MaterialColor.onSurfaceVariant
                    )
                ) {
                    Text(text = if (isFullScreen) "退出全屏" else "全屏")
                }
            }
        }
    }
}

private fun formatElapsedTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

@Composable
private fun SettlementDialog(
    rawDurationMin: Int,
    netDurationMin: Int,
    interruptCount: Int = 0,
    lastPauseReason: String? = null,
    description: String,
    onNetDurationChange: (Int) -> Unit,
    onAdjustNetDuration: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val ratio = if (rawDurationMin > 0) netDurationMin.toFloat() / rawDurationMin.toFloat() else 0f

    AlertDialog(
        onDismissRequest = onCancel,
        shape = DialogShape,
        title = {
            Text(
                text = "结算",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialColor.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)) {
                Text(
                    text = "原始时长：${rawDurationMin} 分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.onSurfaceVariant
                )

                // #11 中断次数 + #12 暂停原因 在结算对话框中显示
                if (interruptCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShapeSmall)
                            .background(MaterialColor.primaryContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = SushiIcons.Sync,
                            contentDescription = null,
                            tint = MaterialColor.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "本次被打断 $interruptCount 次" + if (!lastPauseReason.isNullOrBlank()) " · 最近：$lastPauseReason" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialColor.primary
                        )
                    }
                }

                Text(
                    text = "请诚实地扣除杂质时间，记录纯时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.onSurface
                )

                Slider(
                    value = netDurationMin.toFloat(),
                    onValueChange = { onNetDurationChange(it.toInt()) },
                    valueRange = 0f..rawDurationMin.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialColor.primary,
                        activeTrackColor = MaterialColor.primary,
                        inactiveTrackColor = MaterialColor.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                ) {
                    listOf(-5, -10, -15).forEach { adjustment ->
                        OutlinedButton(
                            onClick = { onAdjustNetDuration(adjustment) },
                            enabled = netDurationMin + adjustment >= 0,
                            shape = CardShapeSmall,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialColor.surfaceVariant,
                                contentColor = MaterialColor.onSurfaceVariant,
                                disabledContainerColor = MaterialColor.surfaceVariant,
                                disabledContentColor = MaterialColor.outline
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "${adjustment}m")
                        }
                    }
                }

                Text(
                    text = "$netDurationMin 分钟",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialColor.onSurface
                )

                LinearProgressIndicator(
                    progress = ratio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialColor.primary,
                    trackColor = MaterialColor.surfaceVariant,
                )

                Text(
                    text = "做了什么",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialColor.onSurfaceVariant
                )
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text("记录你做了什么…", color = MaterialColor.outline) },
                    singleLine = false,
                    maxLines = 3,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialColor.surfaceVariant,
                        unfocusedContainerColor = MaterialColor.surfaceVariant,
                        cursorColor = MaterialColor.onSurface,
                        focusedIndicatorColor = MaterialColor.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = netDurationMin > 0,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialColor.primary,
                    contentColor = MaterialColor.onPrimary,
                    disabledContainerColor = MaterialColor.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialColor.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "确认结算",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "取消", color = MaterialColor.onSurfaceVariant)
            }
        },
        containerColor = MaterialColor.surface
    )
}

@Composable
private fun SettlementResultDialog(
    result: SettlementResult.Success,
    taskCompleted: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "结算完成",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialColor.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                if (taskCompleted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShapeSmall)
                            .background(MaterialColor.primaryContainer)
                            .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                    ) {
                        Icon(
                            imageVector = SushiIcons.Check,
                            contentDescription = null,
                            tint = MaterialColor.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "任务已完成",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialColor.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (result.levelUpEvents.isNotEmpty()) {
                    result.levelUpEvents.forEach { event ->
                        LevelUpItem(event = event)
                    }
                }

                if (result.unlockedAffixes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SushiSpacing.xs))
                    Text(
                        text = "解锁词条",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialColor.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    result.unlockedAffixes.forEach { affix ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(MaterialColor.primaryContainer)
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Star,
                                contentDescription = null,
                                tint = MaterialColor.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = affix.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialColor.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (!taskCompleted && result.levelUpEvents.isEmpty() && result.unlockedAffixes.isEmpty()) {
                    Text(
                        text = "纯时间已记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialColor.onSurfaceVariant
                    )
                }
            }
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
                    text = "好的",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        containerColor = MaterialColor.surface
    )
}

@Composable
private fun LevelUpItem(event: LevelUpEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(MaterialColor.primaryContainer)
            .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Icon(
            imageVector = SushiIcons.KeyboardUp,
            contentDescription = null,
            tint = MaterialColor.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = event.skillName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialColor.onSurface,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "LV ${event.oldLevel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialColor.onSurfaceVariant
        )
        Icon(
            imageVector = SushiIcons.KeyboardRight,
            contentDescription = null,
            tint = MaterialColor.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "${event.newLevel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialColor.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * #21 毕业提示 - 技能达到 LV 100 后询问是否毕业
 */
@Composable
private fun GraduationHintDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "已至 LV 100，是否毕业？",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialColor.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                Text(
                    text = "此技能已至黑曜之境。毕业后此技能将移入「已毕业」分组，并留下你的寄语。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.onSurfaceVariant
                )
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("毕业寄语（可选）", color = MaterialColor.outline) },
                    singleLine = false,
                    maxLines = 3,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialColor.surfaceVariant,
                        unfocusedContainerColor = MaterialColor.surfaceVariant,
                        cursorColor = MaterialColor.onSurface,
                        focusedIndicatorColor = MaterialColor.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(message) },
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialColor.primary,
                    contentColor = MaterialColor.onPrimary
                )
            ) {
                Text(
                    text = "毕业",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "暂不", color = MaterialColor.onSurfaceVariant)
            }
        },
        containerColor = MaterialColor.surface
    )
}
