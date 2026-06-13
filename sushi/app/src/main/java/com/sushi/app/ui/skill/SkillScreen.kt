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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.DialogShape
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkAlpha08
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.ui.theme.SushiSpacing
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
                    onManualInject = viewModel::showManualInject,
                    onDeleteSkill = { viewModel.deleteSkill(uiState.selectedSkill!!.skill.id) },
                    onEditSkillName = { newName ->
                        viewModel.updateSkillName(uiState.selectedSkill!!.skill.id, newName)
                    },
                    onDeleteTimeRecord = viewModel::deleteTimeRecord
                )
            } else {
                SkillListContent(
                    uiState = uiState,
                    onSelectCategory = viewModel::selectCategory,
                    onSelectSkill = viewModel::selectSkill,
                    onCreateSkill = viewModel::showCreateSkill
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

    uiState.manualInjectResult?.let { result ->
        SettlementResultDialog(
            result = result,
            onDismiss = viewModel::dismissManualInjectResult
        )
    }

    // Fix #11: Create skill dialog
    if (uiState.isCreatingSkill) {
        CreateSkillDialog(
            onConfirm = { name, category ->
                viewModel.createSkill(name, category)
            },
            onDismiss = viewModel::hideCreateSkill
        )
    }

    // Fix #23: Add profession dialog
    if (uiState.showAddProfessionDialog) {
        AddProfessionDialog(
            availableProfessions = uiState.availableProfessions,
            onSelect = { professionId ->
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

            // Fix #16: Category stats bar
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                        ) {
                            Text(
                                text = "◎",
                                style = MaterialTheme.typography.displaySmall,
                                color = InkFaintest
                            )
                            Text(
                                text = "暂无技能",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkFaint
                            )
                            Text(
                                text = "在当前分类下还没有技能",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkFaintest
                            )
                        }
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

        // Fix #11: FAB for creating skill
        FloatingActionButton(
            onClick = onCreateSkill,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = SushiSpacing.xl, bottom = SushiSpacing.xl),
            containerColor = Cinnabar,
            contentColor = Paper,
            shape = CircleShape
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
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

// Fix #16: Category stats bar
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .shadow(1.dp, CardShape)
            .background(Linen)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = 14.dp),
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
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(tierColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = display.tierLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = tierColor
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Paper)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(display.progress / 120f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(tierColor.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(display.progress / 120f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(tierColor.copy(alpha = 0.2f))
                        .offset(x = (-1).dp)
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
    onAddProfession: () -> Unit,
    onRemoveProfession: (String) -> Unit,
    onManualInject: () -> Unit,
    onDeleteSkill: () -> Unit,
    onEditSkillName: (String) -> Unit,
    onDeleteTimeRecord: (String) -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)

    // Fix #6: Delete skill confirmation dialog state
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Fix #9: Edit skill name dialog state
    var showEditNameDialog by remember { mutableStateOf(false) }

    // Fix #13: Delete time record confirmation dialog state
    var recordToDelete by remember { mutableStateOf<TimeRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CardShapeSmall)
                    .background(PaperWarm)
                    .clickable(onClick = onBack)
                    .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkLight
                    )
                    Text(
                        text = "返回",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkLight
                    )
                }
            }
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

            // Fix #9: Skill name is tappable to edit
            Text(
                text = detail.skill.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
                color = Ink,
                modifier = Modifier.clickable { showEditNameDialog = true }
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Linen)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(detail.progress / 120f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(tierColor.copy(alpha = 0.55f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(detail.progress / 120f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(tierColor.copy(alpha = 0.2f))
                                .offset(x = (-1).dp)
                        )
                    }
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(InkFaintest)
            )

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
                    // Fix #23: "+" button calls showAddProfessionDialog
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .shadow(0.5.dp, PillShape)
                            .background(Linen)
                            .clickable { onAddProfession() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkFaint
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(InkFaintest)
            )

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
                            Text(
                                text = "◇",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkFaintest
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(InkFaintest)
            )

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
                            Text(
                                text = "◷",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkFaintest
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
                        // Fix #13: HistoryRecordItem with delete button
                        HistoryRecordItem(
                            record = record,
                            onDelete = { recordToDelete = record }
                        )
                    }
                }
            }

            // Fix #6: Delete skill button
            Button(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                shape = CardShape
            ) {
                Text(
                    text = "删除技能",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
        }
    }

    // Fix #6: Delete skill confirmation dialog
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
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
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

    // Fix #9: Edit skill name dialog
    if (showEditNameDialog) {
        var editName by remember { mutableStateOf(detail.skill.name) }

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

    // Fix #13: Delete time record confirmation dialog
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
                    text = "确定要删除这条 ${record.netDurationMin} 分钟的时间记录吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTimeRecord(record.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
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
private fun HistoryRecordItem(
    record: TimeRecord,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

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
                // Fix #13: Delete icon button
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
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

// Fix #11: Create skill dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSkillDialog(
    onConfirm: (String, SkillCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        SkillCategory.COGNITION to "认知",
        SkillCategory.CREATION to "造物",
        SkillCategory.FUNCTION to "功能",
        SkillCategory.STRATEGY to "策略"
    )
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

// Fix #23: Add profession dialog
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SushiSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "◎",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkFaintest
                        )
                        Text(
                            text = "没有可添加的职业",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkFaint
                        )
                    }
                }
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
    var minutes by remember { mutableIntStateOf(30) }
    var description by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    var endDateTime by remember { mutableStateOf(startDateTime + 30 * 60 * 1000L) }

    LaunchedEffect(minutes, startDateTime) {
        endDateTime = startDateTime + minutes * 60 * 1000L
    }

    val startCal = remember { Calendar.getInstance().apply { timeInMillis = startDateTime } }
    val endCal = remember { Calendar.getInstance().apply { timeInMillis = endDateTime } }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val estimatedExp = minutes * 10

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InkFaintest)
                )

                Text(
                    text = "纯时间（分钟）",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkLight
                )
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InkFaintest)
                )

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
                        Text(dateFormat.format(startDateTime), color = Ink)
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
                        Text(timeFormat.format(startDateTime), color = Ink)
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
                        modifier = Modifier.weight(1f),
                        shape = CardShapeSmall
                    ) {
                        Text(timeFormat.format(endDateTime), color = Ink)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InkFaintest)
                )

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(Linen)
                                .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
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
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardShapeSmall)
                                .background(Linen)
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

private fun tierColorFor(tierLabel: String): Color = when (tierLabel) {
    "原石" -> RawStoneGray
    "青铜" -> BronzeCopper
    "赤金" -> AmberGold
    "黑曜石" -> ObsidianBlack
    else -> InkLight
}
