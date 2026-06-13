package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.MaterialColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 年度热力图 - GitHub 风格
 * Material 3 设计:cell 12dp,圆角 2dp,5 级配色
 */
@Composable
fun HeatmapCalendar(
    heatmap: Map<String, Int>,  // dateKey -> totalMin
    modifier: Modifier = Modifier
) {
    if (heatmap.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium, color = MaterialColor.outline)
        }
        return
    }

    val maxVal = heatmap.values.max().coerceAtLeast(60)  // 至少按 60 分钟分级

    val cal = Calendar.getInstance()
    val sortedKeys = heatmap.keys.sorted()

    cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(sortedKeys.first()) ?: Date()

    val rows = 7  // 周日到周六
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)  // 1=Sunday
    val offset = firstDayOfWeek - 1
    val totalDays = heatmap.size
    val totalCells = offset + totalDays
    val cols = (totalCells + 6) / 7

    val cellSize = 12.dp
    val gap = 3.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${sortedKeys.first()} ~ ${sortedKeys.last()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialColor.onSurfaceVariant
            )
            Text(
                text = "${heatmap.values.count { it > 0 }} 天有记录",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialColor.onSurfaceVariant
            )
        }

        // 热力网格
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(((cellSize.value + gap.value) * rows).dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                for (row in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        var col = 0
                        while (col < cols) {
                            val cellIndex = col * rows + row
                            val dataIndex = cellIndex - offset
                            if (dataIndex < 0 || dataIndex >= totalDays) {
                                Box(modifier = Modifier.size(cellSize))
                                col++
                            } else {
                                val key = sortedKeys[dataIndex]
                                val value = heatmap[key] ?: 0
                                val color = heatmapColor(value, maxVal)
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                                col++
                            }
                        }
                    }
                }
            }
        }

        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "少",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialColor.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                listOf(0, 1, 2, 3, 4).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(heatmapColor(level * (maxVal / 4).coerceAtLeast(1), maxVal))
                    )
                }
            }
            Text(
                text = "多",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialColor.onSurfaceVariant
            )
        }
    }
}

private fun heatmapColor(value: Int, maxVal: Int): Color {
    if (value <= 0) return MaterialColor.surfaceVariant
    val ratio = value.toFloat() / maxVal
    return when {
        ratio < 0.25f -> MaterialColor.primaryContainer
        ratio < 0.5f -> MaterialColor.primary.copy(alpha = 0.5f)
        ratio < 0.75f -> MaterialColor.primary.copy(alpha = 0.75f)
        else -> MaterialColor.primary
    }
}
