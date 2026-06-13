package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.AmberGold
import com.sushi.app.ui.theme.BronzeCopper
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.RawStoneGray
import com.sushi.app.ui.theme.SilverGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * 年度热力图 - GitHub 风格
 */
@Composable
fun HeatmapCalendar(
    heatmap: Map<String, Int>,  // dateKey -> totalMin
    modifier: Modifier = Modifier
) {
    if (heatmap.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = InkFaint)
        }
        return
    }

    val maxVal = heatmap.values.max().coerceAtLeast(60)  // 至少按 60 分钟分级

    val cal = Calendar.getInstance()
    val sortedKeys = heatmap.keys.sorted()

    // 找到第一个 dateKey 对应的 Calendar
    cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(sortedKeys.first()) ?: Date()

    val rows = 7  // 周日到周六
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)  // 1=Sunday
    val offset = firstDayOfWeek - 1  // 周日作为列 0
    val totalDays = heatmap.size
    val totalCells = offset + totalDays
    val cols = (totalCells + 6) / 7  // 向上取整

    val cellSize = 11.dp
    val gap = 2.dp

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${sortedKeys.first()} ~ ${sortedKeys.last()}",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
            Text(
                text = "${heatmap.values.count { it > 0 }} 天有记录",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
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
                                        .clip(RoundedCornerShape(2.dp))
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
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                listOf(0, 1, 2, 3, 4).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(heatmapColor(level * (maxVal / 4).coerceAtLeast(1), maxVal))
                    )
                }
            }
            Text(
                text = "多",
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

private fun heatmapColor(value: Int, maxVal: Int): Color {
    if (value <= 0) return InkFaintest.copy(alpha = 0.3f)
    val ratio = value.toFloat() / maxVal
    return when {
        ratio < 0.25f -> CinnabarFaint.copy(alpha = 0.4f)
        ratio < 0.5f -> CinnabarFaint.copy(alpha = 0.7f)
        ratio < 0.75f -> Cinnabar
        else -> Cinnabar
    }
}
