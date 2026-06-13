package com.sushi.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.Linen
import kotlin.math.max
import kotlin.math.min

/**
 * 折线图 - 趋势展示
 */
@Composable
fun TrendLineChart(
    dataPoints: List<Pair<Int, Float>>,  // (label index, value)
    yAxisLabels: List<String> = listOf("0", "中", "高"),
    height: Dp = 180.dp,
    lineColor: Color = Cinnabar,
    fillColor: Color = CinnabarFaint,
    gridColor: Color = InkFaintest
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Linen.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = InkFaint)
        }
        return
    }

    val maxVal = dataPoints.maxOf { it.second }.coerceAtLeast(1f)
    val minVal = 0f
    val range = max(maxVal - minVal, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(height)) {
            // Y 轴标签
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yAxisLabels.reversed().forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = InkFaint
                    )
                }
            }

            // 画布
            Canvas(modifier = Modifier.weight(1f).fillMaxSize()) {
                val w = size.width
                val h = size.height
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                // 网格横线
                for (i in 0..3) {
                    val y = h * i / 3
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 0.8f,
                        pathEffect = dashEffect
                    )
                }

                if (dataPoints.size < 2) return@Canvas

                val stepX = w / (dataPoints.size - 1)
                val points = dataPoints.mapIndexed { i, pair ->
                    val x = stepX * i
                    val y = h * (1f - (pair.second - minVal) / range)
                    Offset(x, y)
                }

                // 填充面积
                val fillPath = Path().apply {
                    moveTo(points[0].x, h)
                    for (p in points) lineTo(p.x, p.y)
                    lineTo(points.last().x, h)
                    close()
                }
                drawPath(path = fillPath, color = fillColor, style = Fill)

                // 主线
                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // 节点
                for (p in points) {
                    drawCircle(color = lineColor, radius = 3.5f, center = p)
                    drawCircle(color = Color.White, radius = 1.5f, center = p)
                }
            }
        }

        // X 轴标签 - 最多显示 5 个等距 label
        val labelStep = max(dataPoints.size / 5, 1)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { i, pair ->
                if (i % labelStep == 0 || i == dataPoints.lastIndex) {
                    Text(
                        text = "${pair.first}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = InkFaint,
                        modifier = Modifier.width(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 饼图 - 技能占比
 */
@Composable
fun PieChart(
    dataPoints: List<Pair<String, Int>>,  // (name, value)
    colors: List<Color>,
    size: Dp = 200.dp
) {
    val total = dataPoints.sumOf { it.second }.coerceAtLeast(1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = size.width
            val h = size.height
            val padding = 8f
            val radius = min(w, h) / 2 - padding
            val centerX = w / 2
            val centerY = h / 2

            var startAngle = -90f

            dataPoints.forEachIndexed { index, (_, value) ->
                val sweep = (value.toFloat() / total) * 360f
                val color = colors[index % colors.size]
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                startAngle += sweep
            }
        }

        // 图例
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dataPoints.forEachIndexed { index, (name, value) ->
                val percent = (value.toFloat() / total * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(colors[index % colors.size])
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${value}分 (${percent}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkFaint
                    )
                }
            }
        }
    }
}

/**
 * 柱状图 - 专注时段分析（24 小时）
 */
@Composable
fun BarChart(
    dataPoints: List<Pair<String, Int>>,
    height: Dp = 160.dp,
    barColor: Color = Cinnabar
) {
    val maxVal = dataPoints.maxOf { it.second }.coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height
            val barCount = dataPoints.size
            val barWidth = w / barCount * 0.6f
            val gap = w / barCount * 0.4f

            dataPoints.forEachIndexed { i, (_, value) ->
                val barHeight = (value.toFloat() / maxVal) * (h - 20f)
                val x = i * (barWidth + gap) + gap / 2
                val y = h - barHeight
                drawRoundRect(
                    color = if (value > 0) barColor else InkFaintest.copy(alpha = 0.3f),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0", "6", "12", "18", "24").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = InkFaint
                )
            }
        }
    }
}

/**
 * 横向条形图 - Top 技能
 */
@Composable
fun HorizontalBarChart(
    dataPoints: List<Pair<String, Int>>,
    barColor: Color = CinnabarLight
) {
    val maxVal = dataPoints.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dataPoints.forEach { (name, value) ->
            val ratio = value.toFloat() / maxVal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    modifier = Modifier.width(80.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(InkFaintest.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(barColor)
                    )
                }
                Text(
                    text = "${value}分",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkFaint
                )
            }
        }
    }
}
