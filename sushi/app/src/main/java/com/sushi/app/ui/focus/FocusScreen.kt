package com.sushi.app.ui.focus

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Skill
import com.sushi.app.data.model.Task
import com.sushi.app.logic.LevelUpEvent
import com.sushi.app.logic.SettlementResult
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.viewmodel.FocusUiState
import com.sushi.app.viewmodel.FocusViewModel
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.settlementResult != null -> {
            SettlementResultDialog(
                result = uiState.settlementResult!!,
                onDismiss = viewModel::dismissSettlementResult
            )
        }
        uiState.showSettlement -> {
            SettlementDialog(
                rawDurationMin = uiState.rawDurationMin,
                netDurationMin = uiState.netDurationMin,
                description = uiState.description,
                onNetDurationChange = viewModel::setNetDuration,
                onAdjustNetDuration = viewModel::adjustNetDuration,
                onDescriptionChange = viewModel::updateDescription,
                onConfirm = viewModel::confirmSettlement,
                onCancel = viewModel::dismissSettlementResult
            )
        }
        uiState.isFocusing -> {
            FocusTimerContent(
                taskName = uiState.currentTaskName ?: "",
                elapsedSeconds = uiState.elapsedSeconds,
                onTick = viewModel::updateElapsedTime,
                onStop = viewModel::stopFocus
            )
        }
        else -> {
            TaskSelectionContent(
                activeTasks = uiState.activeTasks,
                allSkills = uiState.allSkills,
                isCreatingTask = uiState.isCreatingTask,
                onSelectTask = { task, skillId ->
                    viewModel.startFocus(task.id, task.name, skillId)
                },
                onShowCreateTask = viewModel::showCreateTask,
                onHideCreateTask = viewModel::hideCreateTask,
                onCreateTask = viewModel::createTask
            )
        }
    }
}

@Composable
private fun TaskSelectionContent(
    activeTasks: List<Task>,
    allSkills: List<Skill>,
    isCreatingTask: Boolean,
    onSelectTask: (Task, String) -> Unit,
    onShowCreateTask: () -> Unit,
    onHideCreateTask: () -> Unit,
    onCreateTask: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Linen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "选择任务",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (activeTasks.isEmpty()) {
                Text(
                    text = "暂无进行中的任务",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint
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

        OutlinedButton(
            onClick = onShowCreateTask,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Paper,
                contentColor = Ink
            )
        ) {
            Text(text = "+ 新任务")
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
private fun TaskCard(
    task: Task,
    skillName: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink
        )
        if (skillName.isNotBlank()) {
            Text(
                text = skillName,
                style = MaterialTheme.typography.bodySmall,
                color = Cinnabar
            )
        }
    }
}

@Composable
private fun CreateTaskDialog(
    allSkills: List<Skill>,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var selectedSkillId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "新建任务",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("任务名称", color = InkFaint) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = InkFaint
                    )
                )

                Text(
                    text = "绑定技能",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allSkills.forEach { skill ->
                        val isSelected = selectedSkillId == skill.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Linen else Paper)
                                .clickable { selectedSkillId = skill.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Cinnabar else InkFaint)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Ink else InkLight
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskName.isNotBlank() && selectedSkillId.isNotBlank()) {
                        onCreate(taskName.trim(), selectedSkillId)
                    }
                },
                enabled = taskName.isNotBlank() && selectedSkillId.isNotBlank()
            ) {
                Text(text = "创建", color = Cinnabar)
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
private fun FocusTimerContent(
    taskName: String,
    elapsedSeconds: Int,
    onTick: (Int) -> Unit,
    onStop: () -> Unit
) {
    LaunchedEffect(Unit) {
        var seconds = elapsedSeconds
        while (true) {
            delay(1000)
            seconds++
            onTick(seconds)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = taskName,
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight
            )

            Text(
                text = formatElapsedTime(elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = Ink
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Cinnabar)
                    .alpha(breathingAlpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onStop,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Paper,
                    contentColor = Cinnabar
                )
            ) {
                Text(text = "停止")
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
    description: String,
    onNetDurationChange: (Int) -> Unit,
    onAdjustNetDuration: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "结算",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "原始时长：${rawDurationMin} 分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )

                Text(
                    text = "请诚实地扣除杂质时间，记录纯时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink
                )

                Slider(
                    value = netDurationMin.toFloat(),
                    onValueChange = { onNetDurationChange(it.toInt()) },
                    valueRange = 0f..rawDurationMin.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Cinnabar,
                        activeTrackColor = Cinnabar,
                        inactiveTrackColor = Linen
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(-5, -10, -15).forEach { adjustment ->
                        OutlinedButton(
                            onClick = { onAdjustNetDuration(adjustment) },
                            enabled = netDurationMin + adjustment >= 0,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Paper,
                                contentColor = InkLight
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "${adjustment}m")
                        }
                    }
                }

                Text(
                    text = "$netDurationMin 分钟",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink
                )

                // 描述输入
                Text(
                    text = "做了什么",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
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
            TextButton(onClick = onConfirm) {
                Text(text = "确认结算", color = Cinnabar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
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
                text = "结算完成",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (result.levelUpEvents.isNotEmpty()) {
                    result.levelUpEvents.forEach { event ->
                        LevelUpItem(event = event)
                    }
                }

                if (result.unlockedAffixes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
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

@Composable
private fun LevelUpItem(event: LevelUpEvent) {
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
