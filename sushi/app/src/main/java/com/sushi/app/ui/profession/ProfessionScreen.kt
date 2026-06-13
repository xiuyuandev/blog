package com.sushi.app.ui.profession

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.RawStoneGray
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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.selectedProfession != null -> {
                ProfessionDetailContent(
                    detail = uiState.selectedProfession!!,
                    onBack = viewModel::clearSelectedProfession
                )
            }
            else -> {
                ProfessionListContent(
                    uiState = uiState,
                    onSelectProfession = viewModel::selectProfession,
                    onShowCreate = viewModel::showCreate
                )
            }
        }

        if (uiState.isCreating) {
            CreateProfessionDialog(
                onConfirm = { name ->
                    viewModel.createProfession(name)
                },
                onDismiss = viewModel::hideCreate
            )
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "职业身份",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (uiState.professions.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "游侠",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        color = InkLight
                    )
                    Text(
                        text = "你尚未选择职业，自由如风",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint
                    )
                }
            } else {
                uiState.professions.forEach { display ->
                    ProfessionCard(
                        display = display,
                        onClick = { onSelectProfession(display.profession.id) }
                    )
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .background(Ink, CircleShape)
                .clickable(onClick = onShowCreate)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = Paper
            )
        }
    }
}

@Composable
private fun ProfessionCard(
    display: ProfessionDisplay,
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
                text = display.profession.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif
                ),
                color = Ink
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${display.level}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif
                    ),
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
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .background(Paper)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(display.progress / 120f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
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
private fun ProfessionDetailContent(
    detail: ProfessionDetail,
    onBack: () -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        // Back button
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
            // Level and tier
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${detail.level}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = Ink
                )
                Text(
                    text = detail.tierLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = tierColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Profession name
            Text(
                text = detail.profession.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif
                ),
                color = Ink
            )

            // Total pure time
            val hours = detail.totalPureTimeMin / 60
            val minutes = detail.totalPureTimeMin % 60
            val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "素时累计",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = Ink
                )
            }

            // Progress bar
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
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(Linen)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(detail.progress / 120f)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
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

            // Core skills matrix
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CoreSkillItem(
    skillWithProgress: SkillWithProgress
) {
    val tierColor = tierColorFor(tierLabelForLevel(skillWithProgress.level))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = skillWithProgress.skill.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(Paper)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(skillWithProgress.progress / 120f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        .background(tierColor.copy(alpha = 0.55f))
                )
            }
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
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = "职业名称",
                        color = InkFaint
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ink,
                    unfocusedBorderColor = InkLight,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim())
                    }
                }
            ) {
                Text(
                    text = "确认",
                    color = Ink
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = InkLight
                )
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
    level >= 40 -> "黑曜石"
    level >= 25 -> "赤金"
    level >= 10 -> "青铜"
    else -> "原石"
}
