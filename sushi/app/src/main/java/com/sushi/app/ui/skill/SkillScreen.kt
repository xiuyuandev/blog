package com.sushi.app.ui.skill

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.Profession
import com.sushi.app.data.model.SkillCategory
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
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
                    }
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
private fun SkillDetailContent(
    detail: SkillDetail,
    onBack: () -> Unit,
    onAddProfession: (String) -> Unit,
    onRemoveProfession: (String) -> Unit
) {
    val tierColor = tierColorFor(detail.tierLabel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        // Back button area
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

            // Skill name
            Text(
                text = detail.skill.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif
                ),
                color = Ink
            )

            // Experience progress
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

            // Professions
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
                    // Add button
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

            // Affix tree
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "词条",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )

                // Unlocked affixes
                detail.unlockedAffixes.forEach { affix ->
                    AffixItem(
                        affix = affix,
                        isUnlocked = true,
                        tierColor = tierColor
                    )
                }

                // Locked affixes
                detail.lockedAffixes.forEach { affix ->
                    AffixItem(
                        affix = affix,
                        isUnlocked = false,
                        tierColor = tierColor
                    )
                }

                if (detail.unlockedAffixes.isEmpty() && detail.lockedAffixes.isEmpty()) {
                    Text(
                        text = "暂无词条",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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

private fun tierColorFor(tierLabel: String): Color = when (tierLabel) {
    "原石" -> RawStoneGray
    "青铜" -> BronzeCopper
    "赤金" -> AmberGold
    "黑曜石" -> ObsidianBlack
    else -> InkLight
}
