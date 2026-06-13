package com.sushi.app.ui.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.components.AchievementBadge
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.components.SushiProgressBar
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.SushiSpacing

@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "成就",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink
                )
                Text(
                    text = "已解锁 ${uiState.unlockedCount} / ${uiState.totalCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint
                )
            }

            SushiProgressBar(
                progress = if (uiState.totalCount > 0) uiState.unlockedCount.toFloat() / uiState.totalCount else 0f,
                color = Cinnabar,
                size = 6.dp
            )

            Spacer(modifier = Modifier.height(SushiSpacing.sm))

            // 按 tier 分组展示
            val grouped = uiState.achievements.groupBy { it.tierLabel }
            listOf("金", "银", "铜").forEach { tier ->
                val list = grouped[tier].orEmpty()
                if (list.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                        Text(
                            text = "$tier 成就",
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                            list.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
                                ) {
                                    row.forEach { ach ->
                                        AchievementBadge(
                                            name = ach.name,
                                            description = ach.description,
                                            tier = ach.tierLabel,
                                            isUnlocked = ach.isUnlocked,
                                            progress = ach.currentProgress,
                                            threshold = ach.threshold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
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
