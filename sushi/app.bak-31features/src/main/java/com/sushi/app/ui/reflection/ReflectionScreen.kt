package com.sushi.app.ui.reflection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.DailyReflection
import com.sushi.app.ui.components.MoodPicker
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiFab
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.SushiSpacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@Composable
fun ReflectionScreen(
    viewModel: ReflectionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                Column {
                    Text(
                        text = "今日反思",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink
                    )
                    Text(
                        text = "今日 ${uiState.todayKey}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                }

                // 引导问题
                val questions = listOf(
                    "今天最值得记录的时刻" to "highlight",
                    "今天学到了什么" to "lesson",
                    "明天可以改进什么" to "improvement"
                )
                questions.forEach { (q, _) ->
                    Text(
                        text = "• $q？",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkLight
                    )
                }

                if (uiState.todayReflection != null) {
                    Text(
                        text = "今日反思已记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cinnabar
                    )
                } else {
                    SushiFab(
                        onClick = { viewModel.showEditDialog() },
                        icon = SushiIcons.Add,
                        contentDescription = "写反思"
                    )
                }

                Spacer(modifier = Modifier.height(SushiSpacing.xxl))

                // 历史
                Text(
                    text = "历史反思",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink
                )
                if (uiState.pastReflections.isEmpty()) {
                    Text(
                        text = "暂无历史反思",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                } else {
                    uiState.pastReflections.forEach { r ->
                        ReflectionItem(reflection = r, onClick = { viewModel.editReflection(r) })
                    }
                }

                Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
            }
        }

        if (uiState.showEditDialog) {
            EditReflectionDialog(
                existing = uiState.editingReflection,
                onConfirm = viewModel::saveReflection,
                onDismiss = viewModel::hideEditDialog
            )
        }

        if (uiState.isLoading) {
            SushiLoading(text = "加载中")
        }
    }
}

@Composable
private fun ReflectionItem(reflection: DailyReflection, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reflection.dateKey,
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink
                )
                Text(
                    text = "心情 ${reflection.moodScore}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinnabar
                )
            }
            if (reflection.highlight.isNotBlank()) {
                Text(
                    text = "亮点：${reflection.highlight}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight,
                    maxLines = 2
                )
            }
            if (reflection.lesson.isNotBlank()) {
                Text(
                    text = "所学：${reflection.lesson}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EditReflectionDialog(
    existing: DailyReflection?,
    onConfirm: (String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var highlight by rememberSaveable(existing) { mutableStateOf(existing?.highlight ?: "") }
    var lesson by rememberSaveable(existing) { mutableStateOf(existing?.lesson ?: "") }
    var improvement by rememberSaveable(existing) { mutableStateOf(existing?.improvement ?: "") }
    var mood by rememberSaveable(existing) { mutableStateOf(existing?.moodScore ?: 3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = { Text("反思", style = MaterialTheme.typography.headlineSmall, color = Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = highlight,
                    onValueChange = { highlight = it },
                    label = { Text("亮点", color = InkLight) },
                    shape = CardShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cinnabar,
                        unfocusedBorderColor = InkFaint,
                        cursorColor = Ink,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lesson,
                    onValueChange = { lesson = it },
                    label = { Text("所学", color = InkLight) },
                    shape = CardShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cinnabar,
                        unfocusedBorderColor = InkFaint,
                        cursorColor = Ink,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = improvement,
                    onValueChange = { improvement = it },
                    label = { Text("明日改进", color = InkLight) },
                    shape = CardShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cinnabar,
                        unfocusedBorderColor = InkFaint,
                        cursorColor = Ink,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("心情", style = MaterialTheme.typography.labelLarge, color = InkLight)
                MoodPicker(moodScore = mood, onMoodChange = { mood = it })
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(highlight, lesson, improvement, mood) },
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper
                )
            ) {
                Text("保存", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = InkLight) }
        },
        containerColor = Paper
    )
}
