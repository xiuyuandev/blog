package com.sushi.app.ui.profession

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.Profession
import com.sushi.app.ui.components.MenuAction
import com.sushi.app.ui.components.StampDeleteButton
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiFab
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.components.SushiOverflowMenu
import com.sushi.app.ui.components.SushiProgressBar
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
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.util.HapticType
import com.sushi.app.util.rememberHaptic
import com.sushi.app.viewmodel.ProfessionDetail
import com.sushi.app.viewmodel.ProfessionDisplay
import com.sushi.app.viewmodel.ProfessionUiState
import com.sushi.app.viewmodel.ProfessionViewModel
import com.sushi.app.viewmodel.SkillWithProgress

@Composable
fun ProfessionScreen(
    viewModel: ProfessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHaptic()

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = uiState.selectedProfession != null,
            animationSpec = tween(durationMillis = 300),
            label = "professionCrossfade"
        ) { hasDetail ->
            if (hasDetail && uiState.selectedProfession != null) {
                ProfessionDetailContent(
                    detail = uiState.selectedProfession!!,
                    onBack = viewModel::clearSelectedProfession,
                    onDeleteProfession = { id ->
                        haptic(HapticType.REJECT)
                        viewModel.deleteProfession(id)
                    },
                    onUpdateProfessionName = { id, name ->
                        haptic(HapticType.CONFIRM)
                        viewModel.updateProfessionName(id, name)
                    }
                )
            } else {
                ProfessionListContent(
                    uiState = uiState,
                    onSelectProfession = { id ->
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.selectProfession(id)
                    },
                    onShowCreate = {
                        haptic(HapticType.KEYBOARD_TAP)
                        viewModel.showCreate()
                    }
                )
            }
        }

        if (uiState.isCreating) {
            CreateProfessionDialog(
                onConfirm = { name ->
                    haptic(HapticType.CONFIRM)
                    viewModel.createProfession(name)
                },
                onDismiss = viewModel::hideCreate
            )
        }

        if (uiState.isLoading) {
            SushiLoading(text = "加载中")
        }
    }
}

@Composable
private fun ProfessionListContent(
    uiState: ProfessionUiState,
    onSelectProfession: (String) -> Unit,
    onShowCreate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
        ) {
            Text(
                text = "职业身份",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink,
                modifier = Modifier.padding(bottom = SushiSpacing.xs)
            )

            if (uiState.professions.isEmpty() && !uiState.isLoading) {
                EmptyProfessionState(onAction = onShowCreate)
            } else {
                uiState.professions.forEach { display ->
                    ProfessionCard(
                        display = display,
                        onClick = { onSelectProfession(display.profession.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        SushiFab(
            onClick = onShowCreate,
            icon = SushiIcons.Add,
            contentDescription = "新建职业"
        )
    }
}

@Composable
private fun EmptyProfessionState(onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Text(
            text = "游侠",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light
            ),
            color = InkLight
        )
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 1.dp)
                .background(InkFaintest)
        )
        Text(
            text = "你尚未选择职业，自由如风",
            style = MaterialTheme.typography.bodyMedium,
            color = InkFaint
        )
        Spacer(modifier = Modifier.height(SushiSpacing.md))
        Button(
            onClick = onAction,
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cinnabar,
                contentColor = Paper
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
            ) {
                Icon(
                    imageVector = SushiIcons.Add,
                    contentDescription = null,
                    tint = Paper,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "新建职业",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ProfessionCard(
    display: ProfessionDisplay,
    onClick: () -> Unit
) {
    val tierColor = tierColorFor(display.tierLabel)
    val progress = (display.progress / 120f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
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
                text = display.profession.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif
                ),
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
private fun ProfessionDetailContent(
    detail: ProfessionDetail,
    onBack: () -> Unit,
    onDeleteProfession: (String) -> Unit,
    onUpdateProfessionName: (String, String) -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)
    val progress = (detail.progress / 120f).coerceIn(0f, 1f)

    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showEditName by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
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
                        onClick = { showEditName = true }
                    ),
                    MenuAction(
                        label = "删除职业",
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
            // Level and tier
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md)
            ) {
                Text(
                    text = "${detail.level}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    ),
                    color = Ink
                )
                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .clip(PillShape)
                        .background(tierColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = detail.tierLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = tierColor
                    )
                }
            }

            // Profession name
            Text(
                text = detail.profession.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif
                ),
                color = Ink
            )

            Divider1px()

            // Total pure time
            val hours = detail.totalPureTimeMin / 60
            val minutes = detail.totalPureTimeMin % 60
            val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)) {
                Text(
                    text = "素时累计",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Ink
                )
            }

            Divider1px()

            // Progress bar
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

            Divider1px()

            // Core skills matrix
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                Text(
                    text = "核心技能矩阵",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )

                if (detail.coreSkills.isEmpty()) {
                    Text(
                        text = "暂无核心技能",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                } else {
                    detail.coreSkills.forEach { skillWithProgress ->
                        CoreSkillItem(skillWithProgress)
                    }
                }
            }

            Divider1px()

            // Affixes section
            Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                Text(
                    text = "词条",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )

                if (detail.unlockedAffixes.isEmpty() && detail.lockedAffixes.isEmpty()) {
                    Text(
                        text = "暂无词条",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                } else {
                    detail.unlockedAffixes.forEach { affix ->
                        AffixRow(affix = affix, isUnlocked = true)
                    }
                    detail.lockedAffixes.forEach { affix ->
                        AffixRow(affix = affix, isUnlocked = false)
                    }
                }
            }

            // Delete profession button (stamp style)
            StampDeleteButton(
                onClick = { showDeleteConfirm = true },
                label = "删除职业"
            )

            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = DialogShape,
            title = {
                Text(
                    text = "删除职业",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "确定要删除「${detail.profession.name}」吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteProfession(detail.profession.id)
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

    // Edit name dialog
    if (showEditName) {
        EditProfessionNameDialog(
            currentName = detail.profession.name,
            onConfirm = { newName ->
                showEditName = false
                onUpdateProfessionName(detail.profession.id, newName)
            },
            onDismiss = { showEditName = false }
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
private fun AffixRow(affix: Affix, isUnlocked: Boolean) {
    val contentAlpha = if (isUnlocked) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShapeSmall)
            .background(if (isUnlocked) Linen else PaperWarm)
            .alpha(contentAlpha)
            .padding(horizontal = SushiSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
    ) {
        Icon(
            imageVector = if (isUnlocked) SushiIcons.Check else SushiIcons.Lock,
            contentDescription = null,
            tint = if (isUnlocked) Cinnabar else InkFaint,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = affix.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnlocked) Ink else InkFaint
            )
            Text(
                text = if (isUnlocked) affix.description else "需要技能等级 ${affix.requiredSkillLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint
            )
        }
    }
}

@Composable
private fun EditProfessionNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "编辑职业名称",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink
            )
        },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("职业名称", color = InkFaint) },
                singleLine = true,
                shape = CardShapeSmall,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PaperWarm,
                    unfocusedContainerColor = PaperWarm,
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
                    if (name.isNotBlank()) {
                        onConfirm(name.trim())
                    }
                },
                enabled = name.isNotBlank(),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "确认",
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
private fun CoreSkillItem(
    skillWithProgress: SkillWithProgress
) {
    val tierColor = tierColorFor(tierLabelForLevel(skillWithProgress.level))
    val progress = (skillWithProgress.progress / 120f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShapeSmall)
            .clip(CardShapeSmall)
            .background(Linen)
            .padding(horizontal = SushiSpacing.md, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tierColor)
                )
                Text(
                    text = skillWithProgress.skill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink
                )
            }
            Text(
                text = "LV ${skillWithProgress.level}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink
            )
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
                text = "${skillWithProgress.progress}/120",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

@Composable
private fun CreateProfessionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShape,
        title = {
            Text(
                text = "新建职业",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink
            )
        },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("职业名称", color = InkFaint) },
                singleLine = true,
                shape = CardShapeSmall,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PaperWarm,
                    unfocusedContainerColor = PaperWarm,
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
                    if (name.isNotBlank()) {
                        onConfirm(name.trim())
                    }
                },
                enabled = name.isNotBlank(),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = CinnabarLight.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.5f)
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
                Text(text = "取消", color = InkLight)
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

private fun tierLabelForLevel(level: Int): String = when {
    level >= 100 -> "黑曜石"
    level >= 30 -> "赤金"
    level >= 10 -> "青铜"
    else -> "原石"
}
