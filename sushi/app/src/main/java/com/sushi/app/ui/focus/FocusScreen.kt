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
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Skill
import com.sushi.app.data.model.Task
import com.sushi.app.logic.LevelUpEvent
import com.sushi.app.logic.SettlementResult
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.SushiSpacing
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
                onCancel = viewModel::dismissSettlement
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
                        taskName = uiState.currentTaskName ?: "",
                        elapsedSeconds = uiState.elapsedSeconds,
                        onTick = viewModel::updateElapsedTime,
                        onStop = viewModel::stopFocus
                    )
                } else {
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
                .padding(horizontal = SushiSpacing.xxl, vertical = SushiSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
        ) {
            Text(
                text = "选择任务",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
            )

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            if (activeTasks.isEmpty()) {
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
                        Text(
                            text = "暂无进行中的任务",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkLight
                        )
                        Text(
                            text = "点击下方按钮创建一个新任务吧",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkFaint
                        )
                    }
                }
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

        Button(
            onClick = onShowCreateTask,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SushiSpacing.xxxl)
                .shadow(2.dp, CardShape),
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cinnabar,
                contentColor = Paper
            )
        ) {
            Text(
                text = "+ 新任务",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
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
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
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
        shape = DialogShape,
        title = {
            Text(
                text = "新建任务",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("任务名称", color = InkFaint) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PaperWarm,
                        unfocusedContainerColor = PaperWarm,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "绑定技能",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
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
                                .background(if (isSelected) CinnabarFaint else PaperWarm)
                                .clickable { selectedSkillId = skill.id }
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Cinnabar else InkFaint)
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .padding(4.dp)
                                                .clip(CircleShape)
                                                .background(Paper)
                                        } else Modifier
                                    )
                            )
                            Spacer(modifier = Modifier.width(SushiSpacing.sm))
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

    val progressAngle = ((elapsedSeconds % 3600) / 3600f) * 360f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
        ) {
            Text(
                text = "专注中",
                style = MaterialTheme.typography.labelLarge,
                color = Cinnabar,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = taskName,
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight
            )

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            Box(
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 3.dp.toPx()
                    val arcPadding = strokeWidth / 2
                    drawArc(
                        color = Linen,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Cinnabar,
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
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Cinnabar.copy(alpha = 0.15f))
                        .alpha(breathingAlpha)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Cinnabar)
                        .alpha(breathingAlpha)
                )
            }

            Spacer(modifier = Modifier.height(SushiSpacing.lg))

            OutlinedButton(
                onClick = onStop,
                shape = CardShape,
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
    val ratio = if (rawDurationMin > 0) netDurationMin.toFloat() / rawDurationMin.toFloat() else 0f

    AlertDialog(
        onDismissRequest = onCancel,
        shape = DialogShape,
        title = {
            Text(
                text = "结算",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)) {
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
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                ) {
                    listOf(-5, -10, -15).forEach { adjustment ->
                        OutlinedButton(
                            onClick = { onAdjustNetDuration(adjustment) },
                            enabled = netDurationMin + adjustment >= 0,
                            shape = CardShapeSmall,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PaperWarm,
                                contentColor = InkLight,
                                disabledContainerColor = PaperWarm,
                                disabledContentColor = InkFaint
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
                    color = Ink
                )

                LinearProgressIndicator(
                    progress = ratio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Cinnabar,
                    trackColor = Linen,
                )

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
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PaperWarm,
                        unfocusedContainerColor = PaperWarm,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
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
        shape = DialogShape,
        title = {
            Text(
                text = "结算完成",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
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
                        color = Cinnabar,
                        fontWeight = FontWeight.SemiBold
                    )
                    result.unlockedAffixes.forEach { affix ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(CinnabarFaint)
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
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
                                color = Ink,
                                fontWeight = FontWeight.Medium
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(CinnabarFaint)
            .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Text(
            text = event.skillName,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "LV ${event.oldLevel}",
            style = MaterialTheme.typography.bodyMedium,
            color = InkLight
        )
        Text(
            text = "→",
            style = MaterialTheme.typography.bodyMedium,
            color = Cinnabar,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${event.newLevel}",
            style = MaterialTheme.typography.bodyMedium,
            color = Cinnabar,
            fontWeight = FontWeight.Bold
        )
    }
}
