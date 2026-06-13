package com.sushi.app.ui.skill

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.Profession
import com.sushi.app.data.model.SkillCategory
import com.sushi.app.data.model.TimeRecord
import com.sushi.app.logic.LevelUpEvent
import com.sushi.app.logic.SettlementResult
import com.sushi.app.ui.components.MenuAction
import com.sushi.app.ui.components.StampDeleteButton
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiFab
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.components.SushiOverflowMenu
import com.sushi.app.ui.components.SushiProgressBar
import com.sushi.app.ui.components.UndoSnackbar
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
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.util.HapticType
import com.sushi.app.util.rememberHaptic
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
    val haptic = rememberHaptic()

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = uiState.selectedSkill != null,
            animationSpec = tween(durationMillis = 300),
            label = "skillCrossfade"
        ) { isDetail ->
            if (isDetail && uiState.selectedSkill != null) {
                SkillDetailContent(
                    detail = uiState.selectedSkill!!,
                    onBack = viewModel::clearSelectedSkill,
                    onAddProfession = viewModel::showAddProfessionDialog,
                    onRemoveProfession = { professionId ->
                        viewModel.removeProfessionFromSkill(
                            uiState.selectedSkill!!.skill.id,
                            professionId
                        )
                    },
                    onManualInject = {
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.showManualInject()
                    },
                    onDeleteSkill = {
                        haptic(HapticType.LONG_PRESS)
                        viewModel.deleteSkill(uiState.selectedSkill!!.skill.id)
                    },
                    onEditSkillName = { newName ->
                        haptic(HapticType.CONFIRM)
                        viewModel.updateSkillName(uiState.selectedSkill!!.skill.id, newName)
                    },
                    onDeleteTimeRecord = { id ->
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.deleteTimeRecord(id)
                    }
                )
            } else {
                SkillListContent(
                    uiState = uiState,
                    onSelectCategory = viewModel::selectCategory,
                    onSelectSkill = { id ->
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.selectSkill(id)
                    },
                    onCreateSkill = {
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.showCreateSkill()
                    }
                )
            }
        }

        if (uiState.isLoading) {
            SushiLoading(text = "加载中")
        }
    }

    if (uiState.showManualInject && uiState.selectedSkill != null) {
        ManualInjectDialog(
            skillName = uiState.selectedSkill!!.skill.name,
            onConfirm = { minutes, startDt, endDt, desc ->
                haptic(HapticType.CONFIRM)
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

    uiState.manualInjectResult?.let { result ->
        SettlementResultDialog(
            result = result,
            onDismiss = {
                haptic(HapticType.LONG_PRESS)
                viewModel.dismissManualInjectResult()
            }
        )
    }

    if (uiState.isCreatingSkill) {
        CreateSkillDialog(
            onConfirm = { name, category ->
                haptic(HapticType.CONFIRM)
                viewModel.createSkill(name, category)
            },
            onDismiss = viewModel::hideCreateSkill
        )
    }

    if (uiState.showAddProfessionDialog) {
        AddProfessionDialog(
            availableProfessions = uiState.availableProfessions,
            onSelect = { professionId ->
                haptic(HapticType.CONFIRM)
                viewModel.addProfessionToSelectedSkill(professionId)
            },
            onDismiss = viewModel::hideAddProfessionDialog
        )
    }
}

@Composable
private fun SkillListContent(
    uiState: SkillUiState,
    onSelectCategory: (SkillCategory) -> Unit,
    onSelectSkill: (String) -> Unit,
    onCreateSkill: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
        ) {
            CategoryTabs(
                selectedCategory = uiState.selectedCategory,
                onSelectCategory = onSelectCategory
            )

            CategoryStatsBar(
                selectedCategory = uiState.selectedCategory,
                categoryStats = uiState.categoryStats
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
            ) {
                if (uiState.skills.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        icon = SushiIcons.Inventory,
                        title = "暂无技能",
                        subtitle = "在当前分类下还没有技能",
                        actionLabel = "+ 新建技能",
                        onAction = onCreateSkill
                    )
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

        SushiFab(
            onClick = onCreateSkill,
            icon = SushiIcons.Add,
            contentDescription = "新建技能"
        )
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
            .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEachIndexed { index, (category, label) ->
            val isSelected = category == selectedCategory
            val animatedIndicatorWidth by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 0.dp,
                animationSpec = tween(durationMillis = 250),
                label = "tabIndicatorWidth_$index"
            )
            val animatedIndicatorColor by animateColorAsState(
                targetValue = if (isSelected) Ink else Color.Transparent,
                animationSpec = tween(durationMillis = 250),
                label = "tabIndicatorColor_$index"
            )
            val animatedTextColor by animateColorAsState(
                targetValue = if (isSelected) Ink else InkFaint,
                animationSpec = tween(durationMillis = 250),
                label = "tabTextColor_$index"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCategory(category) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = animatedTextColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(animatedIndicatorWidth)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(animatedIndicatorColor)
                )
            }
        }
    }
}

@Composable
private fun CategoryStatsBar(
    selectedCategory: SkillCategory,
    categoryStats: Map<SkillCategory, Int>
) {
    val totalExp = categoryStats[selectedCategory] ?: 0
    val categoryLabel = when (selectedCategory) {
        SkillCategory.COGNITION -> "认知"
        SkillCategory.CREATION -> "造物"
        SkillCategory.FUNCTION -> "功能"
        SkillCategory.STRATEGY -> "策略"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperWarm)
            .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${categoryLabel}总经验",
                style = MaterialTheme.typography.labelMedium,
                color = InkLight
            )
            Text(
                text = "$totalExp Exp",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Ink
            )
        }
    }
}

@Composable
private fun SkillCard(
    display: SkillDisplay,
    onClick: () -> Unit
) {
    val tierColor = tierColorFor(display.tierLabel)
    val progress = (display.progress / 120f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .shadow(1.dp, CardShape)
            .background(Linen)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
            ) {
                Text(
                    text = "${display.level}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Ink
                )
                TierPill(label = display.tierLabel, color = tierColor)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            SushiProgressBar(
                progress = progress,
                color = tierColor,
                size = 6.dp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${display.progress}/120",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

@Composable
fun TierPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun SkillDetailContent(
    detail: SkillDetail,
    onBack: () -> Unit,
    onAddProfession: () -> Unit,
    onRemoveProfession: (String) -> Unit,
    onManualInject: () -> Unit,
    onDeleteSkill: () -> Unit,
    onEditSkillName: (String) -> Unit,
    onDeleteTimeRecord: (String) -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)
    val progress = (detail.progress / 120f).coerceIn(0f, 1f)

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<TimeRecord?>(null) }
    var lastDeleted by remember { mutableStateOf<TimeRecord?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back + overflow menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SushiBackButton(onClick = onBack)
                SushiOverflowMenu(
                    actions = listOf(
                        MenuAction(
                            label = "编辑名称",
                            icon = SushiIcons.Edit,
                            onClick = { showEditNameDialog = true }
                        ),
                        MenuAction(
                            label = "删除技能",
                            icon = SushiIcons.Delete,
                            isDestructive = true,
                            onClick = { showDeleteConfirm = true }
                        )
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SushiSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.xxl)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md)
                ) {
                    Text(
                        text = "${detail.level}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(tierColor.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .offset(y = (-8).dp)
                    ) {
                        Text(
                            text = detail.tierLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = tierColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = detail.skill.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
                    color = Ink
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "经验值",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkLight
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                    ) {
                        SushiProgressBar(
                            progress = progress,
                            color = tierColor,
                            size = 8.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${detail.progress}/120",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkFaint
                        )
                    }
                }

                Button(
                    onClick = onManualInject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar,
                        contentColor = Paper
                    ),
                    shape = CardShape
                ) {
                    Text(
                        text = "+ 手动注入时间",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Divider1px()

                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    Text(
                        text = "关联职业",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkLight
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm),
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
                                .clip(PillShape)
                                .shadow(0.5.dp, PillShape)
                                .background(Linen)
                                .clickable { onAddProfession() }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = SushiIcons.Add,
                                    contentDescription = null,
                                    tint = InkFaint,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "添加",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkFaint
                                )
                            }
                        }
                    }
                }

                Divider1px()

                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    Text(
                        text = "词条",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkLight
                    )

                    if (detail.unlockedAffixes.isEmpty() && detail.lockedAffixes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(PaperWarm)
                                .padding(SushiSpacing.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = SushiIcons.Lock,
                                    contentDescription = null,
                                    tint = InkFaintest,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "暂无词条",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkFaint
                                )
                            }
                        }
                    } else {
                        detail.unlockedAffixes.forEach { affix ->
                            AffixItem(affix = affix, isUnlocked = true, tierColor = tierColor)
                        }

                        detail.lockedAffixes.forEach { affix ->
                            AffixItem(affix = affix, isUnlocked = false, tierColor = tierColor)
                        }
                    }
                }

                Divider1px()

                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    Text(
                        text = "历史记录",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkLight
                    )

                    if (detail.historyRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(PaperWarm)
                                .padding(SushiSpacing.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = SushiIcons.History,
                                    contentDescription = null,
                                    tint = InkFaintest,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "暂无记录",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkFaint
                                )
                            }
                        }
                    } else {
                        detail.historyRecords.forEach { record ->
                            HistoryRecordItem(
                                record = record,
                                onDelete = {
                                    lastDeleted = record
                                    recordToDelete = record
                                }
                            )
                        }
                    }
                }

                StampDeleteButton(
                    onClick = { showDeleteConfirm = true },
                    label = "删除技能"
                )

                Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
            }
        }

        // Undo Snackbar 覆盖在底部
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            UndoSnackbar(
                visible = showUndoSnackbar,
                message = lastDeleted?.let { "已删除 ${it.netDurationMin} 分钟记录" } ?: "",
                onUndo = {
                    lastDeleted?.let { onDeleteTimeRecord(it.id) }
                },
                onDismiss = { showUndoSnackbar = false }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = DialogShape,
            title = {
                Text(
                    text = "确认删除",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "确定要删除技能「${detail.skill.name}」吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSkill()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar,
                        contentColor = Paper
                    ),
                    shape = CardShape
                ) {
                    Text(text = "删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "取消", color = InkLight)
                }
            },
            containerColor = Paper
        )
    }

    if (showEditNameDialog) {
        var editName by remember(detail.skill.id) { mutableStateOf(detail.skill.name) }

        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            shape = DialogShape,
            title = {
                Text(
                    text = "编辑技能名称",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
            },
            text = {
                TextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = InkFaint
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onEditSkillName(editName.trim())
                        }
                        showEditNameDialog = false
                    },
                    enabled = editName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar,
                        contentColor = Paper,
                        disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                        disabledContentColor = Paper.copy(alpha = 0.5f)
                    ),
                    shape = CardShape
                ) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(text = "取消", color = InkLight)
                }
            },
            containerColor = Paper
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            shape = DialogShape,
            title = {
                Text(
                    text = "确认删除",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "确定要删除这条 ${record.netDurationMin} 分钟的时间记录吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTimeRecord(record.id)
                        recordToDelete = null
                        showUndoSnackbar = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar,
                        contentColor = Paper
                    ),
                    shape = CardShape
                ) {
                    Text(text = "删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(text = "取消", color = InkLight)
                }
            },
            containerColor = Paper
        )
    }
}

@Composable
private fun Divider1px() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(InkFaintest)
    )
}

@Composable
private fun HistoryRecordItem(
    record: TimeRecord,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(Linen)
            .padding(horizontal = SushiSpacing.md, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${record.netDurationMin}分钟纯时间",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Ink
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
            ) {
                if (record.isManualEntry) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Cinnabar.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "手动",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cinnabar
                        )
                    }
                }
                Icon(
                    imageVector = SushiIcons.Delete,
                    contentDescription = "删除",
                    tint = Cinnabar,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete)
                        .padding(2.dp)
                )
            }
        }
        Text(
            text = "${dateFormat.format(record.startDateTime)} → ${dateFormat.format(record.endDateTime)}",
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
            .clip(PillShape)
            .shadow(0.5.dp, PillShape)
            .background(Linen)
            .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = profession.name,
            style = MaterialTheme.typography.labelMedium,
            color = Ink
        )
        Icon(
            imageVector = SushiIcons.KeyboardRight,
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove)
        )
    }
}

@Composable
private fun AffixItem(
    affix: Affix,
    isUnlocked: Boolean,
    tierColor: Color
) {
    val contentAlpha = if (isUnlocked) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(if (isUnlocked) Linen else PaperWarm)
            .alpha(contentAlpha)
            .padding(horizontal = SushiSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(tierColor)
            )
        }
        Icon(
            imageVector = if (isUnlocked) SushiIcons.Check else SushiIcons.Lock,
            contentDescription = null,
            tint = if (isUnlocked) tierColor else InkFaint,
            modifier = Modifier.size(14.dp)
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
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(InkFaintest)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LV${affix.requiredSkillLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkFaint
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkFaintest,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = InkFaint
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = InkFaintest
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(SushiSpacing.md))
            Button(
                onClick = onAction,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSkillDialog(
    onConfirm: (String, SkillCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        SkillCategory.COGNITION to "认知",
        SkillCategory.CREATION to "造物",
        SkillCategory.FUNCTION to "功能",
        SkillCategory.STRATEGY to "策略"
    )
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "创建新技能",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)) {
                Text(
                    text = "技能名称",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("输入技能名称…", color = InkFaint) },
                    singleLine = true,
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = InkFaint
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "分类",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = categories[selectedCategoryIndex].second,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = CardShapeSmall,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            focusedIndicatorColor = Cinnabar,
                            unfocusedIndicatorColor = InkFaint
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEachIndexed { index, (_, label) ->
                            DropdownMenuItem(
                                text = { Text(text = label, color = Ink) },
                                onClick = {
                                    selectedCategoryIndex = index
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), categories[selectedCategoryIndex].first)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.5f)
                ),
                shape = CardShape
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
                Text(text = "取消", color = InkLight)
            }
        },
        containerColor = Paper
    )
}

@Composable
private fun AddProfessionDialog(
    availableProfessions: List<Profession>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "添加关联职业",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            if (availableProfessions.isEmpty()) {
                EmptyState(
                    icon = SushiIcons.Work,
                    title = "没有可添加的职业",
                    subtitle = "请先创建一个职业"
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                ) {
                    availableProfessions.forEach { profession ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(Linen)
                                .clickable { onSelect(profession.id) }
                                .padding(horizontal = SushiSpacing.md, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profession.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = InkLight)
            }
        },
        containerColor = Paper
    )
}

@Composable
private fun ManualInjectDialog(
    skillName: String,
    onConfirm: (Int, Long, Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    var description by rememberSaveable { mutableStateOf("") }
    var startDateTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    var endDateTime by remember { mutableStateOf(startDateTime + 30 * 60 * 1000L) }

    LaunchedEffect(minutes, startDateTime) {
        endDateTime = startDateTime + minutes * 60 * 1000L
    }

    val startCal = remember { Calendar.getInstance().apply { timeInMillis = startDateTime } }
    val endCal = remember { Calendar.getInstance().apply { timeInMillis = endDateTime } }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val estimatedExp = minutes

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "注入纯时间 → $skillName",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)) {
                // 快捷时间按钮
                Text(
                    text = "纯时间（分钟）",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
                ) {
                    listOf(15, 30, 60, 120).forEach { preset ->
                        OutlinedButton(
                            onClick = { minutes = preset },
                            shape = PillShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (minutes == preset) CinnabarFaint else PaperWarm,
                                contentColor = if (minutes == preset) Cinnabar else InkLight
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${preset}m", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { minutes = (minutes - 5).coerceAtLeast(1) },
                        shape = CardShapeSmall
                    ) { Text("-5") }
                    Text(
                        text = "$minutes",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Ink,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = { minutes += 5 },
                        shape = CardShapeSmall
                    ) { Text("+5") }
                }

                Divider1px()

                Text(
                    text = "开始时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                ) {
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
                        modifier = Modifier.weight(1f),
                        shape = CardShapeSmall
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Calendar,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(dateFormat.format(startDateTime), color = Ink)
                        }
                    }
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
                        modifier = Modifier.weight(1f),
                        shape = CardShapeSmall
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Schedule,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(timeFormat.format(startDateTime), color = Ink)
                        }
                    }
                }

                Text(
                    text = "结束时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
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
                        modifier = Modifier.weight(1f),
                        shape = CardShapeSmall
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Calendar,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(dateFormat.format(endDateTime), color = Ink)
                        }
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
                        modifier = Modifier.weight(1f),
                        shape = CardShapeSmall
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Schedule,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(timeFormat.format(endDateTime), color = Ink)
                        }
                    }
                }

                Divider1px()

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
                    shape = CardShapeSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Ink,
                        focusedIndicatorColor = Cinnabar,
                        unfocusedIndicatorColor = InkFaint
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShapeSmall)
                        .background(Linen)
                        .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$minutes 分钟纯时间 → $estimatedExp Exp",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = InkLight
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(minutes, startDateTime, endDateTime, description)
                },
                enabled = minutes > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.5f)
                ),
                shape = CardShape
            ) {
                Text(
                    text = "确认注入",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
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
        shape = DialogShape,
        title = {
            Text(
                text = "时间已注入",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
                if (result.levelUpEvents.isNotEmpty()) {
                    result.levelUpEvents.forEach { event ->
                        LevelUpRow(event = event)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(CinnabarFaint)
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                        ) {
                            Icon(
                                imageVector = SushiIcons.Star,
                                contentDescription = null,
                                tint = Cinnabar,
                                modifier = Modifier.size(14.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShapeSmall)
                            .background(PaperWarm)
                            .padding(SushiSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "纯时间已记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkLight
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper
                ),
                shape = CardShape
            ) {
                Text(text = "好的")
            }
        },
        containerColor = Paper
    )
}

@Composable
private fun LevelUpRow(event: LevelUpEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(Linen)
            .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
    ) {
        Icon(
            imageVector = SushiIcons.KeyboardUp,
            contentDescription = null,
            tint = Cinnabar,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = event.skillName,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink
        )
        Text(
            text = "LV ${event.oldLevel} → ${event.newLevel}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Cinnabar
        )
    }
}

private fun tierColorFor(tierLabel: String): Color = when (tierLabel) {
    "原石" -> RawStoneGray
    "青铜" -> BronzeCopper
    "赤金" -> AmberGold
    "黑曜石" -> ObsidianBlack
    else -> InkLight
}
