package com.sushi.app.ui.skill.tree

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Skill
import com.sushi.app.logic.ExperienceEngine
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.ObsidianBlack
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.ui.theme.SushiSpacing
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkillTreeScreen(
    viewModel: SkillTreeViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSkillClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
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
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xl)
        ) {
            Text(text = "技能树", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(
                text = "线条表示前置关联，节点大小代表等级",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint
            )

            if (uiState.skills.isEmpty()) {
                Text(
                    text = "暂无技能",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .shadow(1.dp, CardShape)
                        .clip(CardShape)
                        .background(PaperWarm)
                ) {
                    SkillTreeCanvas(
                        skills = uiState.skills,
                        onSkillClick = onSkillClick
                    )
                }

                // 技能列表
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.skills.sortedBy { it.sortOrder }.forEach { skill ->
                        SkillTreeListItem(
                            skill = skill,
                            onClick = { onSkillClick(skill.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
        }

        if (uiState.isLoading) {
            SushiLoading(text = "加载中")
        }
    }
}

@Composable
private fun SkillTreeCanvas(
    skills: List<Skill>,
    onSkillClick: (String) -> Unit
) {
    if (skills.isEmpty()) return
    val sorted = skills.sortedBy { it.sortOrder }
    val centerY = 210f
    val maxX = 800f  // 画布宽
    val spacing = if (sorted.size > 1) maxX / (sorted.size - 1) else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerXLocal = w / 2

            // 画连线 - 前置技能关系
            for (skill in sorted) {
                val nodeX = (skills.indexOf(skill) * (w / sorted.size.coerceAtLeast(1)) + (w / sorted.size.coerceAtLeast(1)) / 2)
                for (prereqId in skill.prerequisiteSkillIds) {
                    val prereq = skills.find { it.id == prereqId } ?: continue
                    val prereqIdx = skills.indexOf(prereq)
                    val prereqX = (prereqIdx * (w / sorted.size.coerceAtLeast(1)) + (w / sorted.size.coerceAtLeast(1)) / 2)
                    drawLine(
                        color = if (prereq in sorted) CinnabarFaint else InkFaintest,
                        start = Offset(prereqX, centerY),
                        end = Offset(nodeX, centerY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f), 0f)
                    )
                }
            }

            // 画节点
            for ((i, skill) in sorted.withIndex()) {
                val x = i * (w / sorted.size.coerceAtLeast(1)) + (w / sorted.size.coerceAtLeast(1)) / 2
                val level = ExperienceEngine().calculateLevel(skill.totalExp)
                val radius = 14f + level * 0.5f
                val tierColor = when {
                    level >= 100 -> ObsidianBlack
                    level >= 30 -> AmberGold
                    level >= 10 -> BronzeCopper
                    else -> RawStoneGray
                }

                drawCircle(
                    color = if (skill.isGraduated) Cinnabar else tierColor,
                    radius = radius,
                    center = Offset(x, centerY)
                )
                drawCircle(
                    color = Paper,
                    radius = radius * 0.5f,
                    center = Offset(x, centerY)
                )
                drawCircle(
                    color = if (skill.isGraduated) Cinnabar else tierColor,
                    radius = radius * 0.4f,
                    center = Offset(x, centerY)
                )
            }
        }
    }
}

@Composable
private fun SkillTreeListItem(skill: Skill, onClick: () -> Unit) {
    val level = ExperienceEngine().calculateLevel(skill.totalExp)
    val tierColor = when {
        level >= 100 -> ObsidianBlack
        level >= 30 -> AmberGold
        level >= 10 -> BronzeCopper
        else -> RawStoneGray
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(if (skill.isGraduated) CinnabarFaint else PaperWarm)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (skill.isGraduated) Cinnabar else tierColor)
            )
            Text(
                text = skill.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (skill.isGraduated) Cinnabar else Ink,
                fontWeight = if (skill.isGraduated) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "LV $level",
                style = MaterialTheme.typography.labelMedium,
                color = if (skill.isGraduated) Cinnabar else Ink
            )
        }
    }
}
