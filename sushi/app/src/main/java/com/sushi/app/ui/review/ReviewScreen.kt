package com.sushi.app.ui.review

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.TimeRecord
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.components.SushiProgressBar
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.SerifFontFamily
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xxl)
        ) {
            CalendarView(
                selectedDate = uiState.selectedDate,
                datesWithRecords = uiState.datesWithRecords,
                onSelectDate = viewModel::selectDate
            )

            AnimatedContent(
                targetState = uiState.selectedDate,
                transitionSpec = {
                    slideInVertically { height -> height / 4 } + fadeIn() togetherWith
                        slideOutVertically { height -> -height / 4 } + fadeOut()
                },
                label = "timeline_transition"
            ) { _ ->
                TimelineSection(
                    records = uiState.recordsByDate,
                    isLoading = uiState.isLoading
                )
            }

            StatisticsSection(
                totalPureTimeMin = uiState.totalPureTimeMin,
                weeklyTotalMin = uiState.weeklyTotalMin,
                monthlyTotalMin = uiState.monthlyTotalMin
            )
        }

        if (uiState.isLoading && uiState.recordsByDate.isEmpty()) {
            SushiLoading(text = "加载中")
        }
    }
}

@Composable
private fun CalendarView(
    selectedDate: Long,
    datesWithRecords: Set<Long>,
    onSelectDate: (Long) -> Unit
) {
    val cal = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }
    val displayCal = remember(cal) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val year = displayCal.get(Calendar.YEAR)
    val month = displayCal.get(Calendar.MONTH)
    val monthNames = listOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )

    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK)
    val startColumn = when (firstDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val selectedCal = remember(selectedDate) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val selectedDayTimestamp = selectedCal.timeInMillis

    val todayTimestamp = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(SushiSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        // Month navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    val prev = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        add(Calendar.MONTH, -1)
                    }
                    onSelectDate(prev.timeInMillis)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = SushiIcons.KeyboardLeft,
                    contentDescription = "上个月",
                    tint = InkLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
            ) {
                Text(
                    text = "${year}年 ${monthNames[month]}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
                // Today button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CinnabarFaint)
                        .clickable { onSelectDate(System.currentTimeMillis()) }
                        .padding(horizontal = SushiSpacing.sm, vertical = SushiSpacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = SushiIcons.Calendar,
                            contentDescription = null,
                            tint = Cinnabar,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "今天",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cinnabar
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    val next = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        add(Calendar.MONTH, 1)
                    }
                    onSelectDate(next.timeInMillis)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = SushiIcons.KeyboardRight,
                    contentDescription = "下个月",
                    tint = InkLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Day of week headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SushiSpacing.xs),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp
                    ),
                    color = InkFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Day grid
        val totalCells = startColumn + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startColumn + 1

                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dayTimestamp = dayCal.timeInMillis
                        val isSelected = dayTimestamp == selectedDayTimestamp
                        val isToday = dayTimestamp == todayTimestamp
                        val hasRecord = datesWithRecords.contains(dayTimestamp)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Background circles
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Cinnabar)
                                )
                            } else if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(InkFaintest.copy(alpha = 0.4f))
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onSelectDate(dayTimestamp) }
                                    )
                                    .padding(SushiSpacing.sm)
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> Paper
                                        isToday -> Cinnabar
                                        else -> Ink
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                // Record indicator dot
                                if (hasRecord) {
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Paper else Cinnabar)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSection(
    records: List<TimeRecord>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(SushiSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Text(
            text = "时间记录",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )

        Crossfade(
            targetState = isLoading to records,
            label = "timeline_content"
        ) { (loading, recs) ->
            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SushiSpacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载中…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkFaint
                        )
                    }
                }
                recs.isEmpty() -> {
                    EmptyTimelineState()
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)) {
                        recs.forEachIndexed { index, record ->
                            TimelineEntry(
                                record = record,
                                isLast = index == recs.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTimelineState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SushiSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Icon(
            imageVector = SushiIcons.History,
            contentDescription = null,
            tint = InkFaintest,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "此日暂无记录",
            style = MaterialTheme.typography.bodyMedium,
            color = InkFaint
        )
        Text(
            text = "选择有记录的日期查看详情",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaintest
        )
    }
}

@Composable
private fun TimelineEntry(record: TimeRecord, isLast: Boolean) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startText = timeFormat.format(record.startDateTime)
    val endText = timeFormat.format(record.endDateTime)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Cinnabar)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(40.dp)
                        .background(InkFaintest)
                )
            }
        }

        // Content card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(CardShapeSmall)
                .background(Paper)
                .padding(SushiSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$startText → $endText",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink
                )
                // Duration badge
                Box(
                    modifier = Modifier
                        .clip(CardShapeSmall)
                        .background(CinnabarFaint)
                        .padding(horizontal = SushiSpacing.sm, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = SushiIcons.Schedule,
                            contentDescription = null,
                            tint = Cinnabar,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = formatDuration(record.netDurationMin),
                            style = MaterialTheme.typography.labelSmall,
                            color = Cinnabar,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (record.description.isNotBlank()) {
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
            }
        }
    }
}

@Composable
private fun StatisticsSection(
    totalPureTimeMin: Int,
    weeklyTotalMin: Int,
    monthlyTotalMin: Int
) {
    val weeklyGoalMin = 2400
    val monthlyGoalMin = 9600
    val weeklyProgress = if (weeklyTotalMin > 0) (weeklyTotalMin.toFloat() / weeklyGoalMin).coerceIn(0f, 1f) else 0f
    val monthlyProgress = if (monthlyTotalMin > 0) (monthlyTotalMin.toFloat() / monthlyGoalMin).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(SushiSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
    ) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )

        // Total pure time
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeSmall)
                .background(CinnabarFaint)
                .padding(SushiSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            Text(
                text = "总累计纯时间",
                style = MaterialTheme.typography.labelLarge,
                color = InkLight
            )
            Text(
                text = if (totalPureTimeMin > 0) formatDuration(totalPureTimeMin) else "暂无数据",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = if (totalPureTimeMin > 0) Cinnabar else InkFaint
            )
        }

        // Weekly stats
        Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
                ) {
                    Icon(
                        imageVector = SushiIcons.Calendar,
                        contentDescription = null,
                        tint = InkLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "本周纯时间",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkLight
                    )
                }
                Text(
                    text = if (weeklyTotalMin > 0) formatDuration(weeklyTotalMin) else "暂无数据",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifFontFamily
                    ),
                    color = if (weeklyTotalMin > 0) Ink else InkFaint
                )
            }
            // 进度条（统一组件）
            SushiProgressBar(
                progress = weeklyProgress,
                color = Cinnabar,
                size = 6.dp
            )
            Text(
                text = if (weeklyTotalMin > 0) "目标 ${formatDuration(weeklyGoalMin)}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InkFaintest.copy(alpha = 0.3f))
        )

        // Monthly stats
        Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
                ) {
                    Icon(
                        imageVector = SushiIcons.History,
                        contentDescription = null,
                        tint = InkLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "本月纯时间",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkLight
                    )
                }
                Text(
                    text = if (monthlyTotalMin > 0) formatDuration(monthlyTotalMin) else "暂无数据",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifFontFamily
                    ),
                    color = if (monthlyTotalMin > 0) Ink else InkFaint
                )
            }
            SushiProgressBar(
                progress = monthlyProgress,
                color = CinnabarLight,
                size = 6.dp
            )
            Text(
                text = if (monthlyTotalMin > 0) "目标 ${formatDuration(monthlyGoalMin)}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

private fun formatDuration(totalMin: Int): String {
    if (totalMin <= 0) return "0m"
    val hours = totalMin / 60
    val minutes = totalMin % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
