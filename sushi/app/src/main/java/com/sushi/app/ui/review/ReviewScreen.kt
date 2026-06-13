package com.sushi.app.ui.review

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.TimeRecord
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.viewmodel.ReviewUiState
import com.sushi.app.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CalendarView(
            selectedDate = uiState.selectedDate,
            datesWithRecords = uiState.datesWithRecords,
            onSelectDate = viewModel::selectDate
        )

        TimelineSection(
            records = uiState.recordsByDate,
            isLoading = uiState.isLoading
        )

        StatisticsSection(
            weeklyTotalMin = uiState.weeklyTotalMin,
            monthlyTotalMin = uiState.monthlyTotalMin
        )
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

    // Monday=1 .. Sunday=7, convert to grid index where column 0 = Monday
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

    // Selected day's start-of-day timestamp
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${year}年 ${monthNames[month]}",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )

        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
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
                        val hasRecord = datesWithRecords.contains(dayTimestamp)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectDate(dayTimestamp) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink,
                                    textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
                                )
                                if (hasRecord) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Cinnabar)
                                    )
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
            .background(Linen)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "时间记录",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )

        if (isLoading) {
            Text(
                text = "加载中…",
                style = MaterialTheme.typography.bodyMedium,
                color = InkFaint
            )
        } else if (records.isEmpty()) {
            Text(
                text = "此日无记录",
                style = MaterialTheme.typography.bodyMedium,
                color = InkFaint
            )
        } else {
            records.forEach { record ->
                TimelineEntry(record = record)
            }
        }
    }
}

@Composable
private fun TimelineEntry(record: TimeRecord) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startText = timeFormat.format(record.startDateTime)
    val endText = timeFormat.format(record.endDateTime)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(InkLight)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(InkFaint)
            )
        }

        // Content
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$startText → $endText",
                style = MaterialTheme.typography.labelLarge,
                color = Ink
            )
            Text(
                text = "${record.netDurationMin}分钟纯时间",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
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
}

@Composable
private fun StatisticsSection(
    weeklyTotalMin: Int,
    monthlyTotalMin: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )

        // Weekly stats
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "本周纯时间",
                style = MaterialTheme.typography.labelLarge,
                color = InkLight
            )
            Text(
                text = if (weeklyTotalMin > 0) "${weeklyTotalMin}分钟" else "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = if (weeklyTotalMin > 0) Ink else InkFaint
            )
        }

        // Monthly stats
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "本月纯时间",
                style = MaterialTheme.typography.labelLarge,
                color = InkLight
            )
            Text(
                text = if (monthlyTotalMin > 0) "${monthlyTotalMin}分钟" else "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = if (monthlyTotalMin > 0) Ink else InkFaint
            )
        }
    }
}
