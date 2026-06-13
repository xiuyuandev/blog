package com.sushi.app.ui.help

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sushi.app.data.model.HelpEntry
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.SushiSpacing

@Composable
fun HelpScreen(
    viewModel: HelpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedEntry by remember { mutableStateOf<HelpEntry?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialColor.surface)
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
            Text(text = "帮助", style = MaterialTheme.typography.headlineMedium, color = MaterialColor.onSurface)

            // 分类筛选
            val categories = listOf("全部") + uiState.entries.map { it.category }.distinct()
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(5).forEach { cat ->
                    val isSelected = (selectedCategory ?: "全部") == cat
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (isSelected) MaterialColor.primaryContainer else MaterialColor.surfaceVariant)
                            .clickable {
                                selectedCategory = if (cat == "全部") null else cat
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialColor.primary else MaterialColor.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            val filtered = uiState.entries.filter {
                selectedCategory == null || it.category == selectedCategory
            }

            if (filtered.isEmpty()) {
                Text(
                    text = "暂无帮助条目",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.outline
                )
            } else {
                filtered.forEach { entry ->
                    HelpEntryCard(entry = entry, onClick = { selectedEntry = entry })
                }
            }

            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
        }

        if (uiState.isLoading) {
            SushiLoading(text = "加载中")
        }
    }

    if (selectedEntry != null) {
        HelpDetailDialog(
            entry = selectedEntry!!,
            onDismiss = { selectedEntry = null }
        )
    }
}

@Composable
private fun HelpEntryCard(entry: HelpEntry, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = entry.title, style = MaterialTheme.typography.bodyMedium, color = MaterialColor.onSurface)
                Text(
                    text = entry.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialColor.primary
                )
            }
            Icon(
                imageVector = SushiIcons.KeyboardRight,
                contentDescription = null,
                tint = MaterialColor.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HelpDetailDialog(entry: HelpEntry, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = {
            Text(entry.title, style = MaterialTheme.typography.headlineSmall, color = MaterialColor.onSurface)
        },
        text = {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.onSurfaceVariant
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("好的", color = MaterialColor.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = MaterialColor.surface
    )
}
