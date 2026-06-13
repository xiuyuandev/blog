package com.sushi.app.ui.panel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.AttributeType
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.viewmodel.PanelUiState
import com.sushi.app.viewmodel.PanelViewModel
import kotlin.math.cos
import kotlin.math.sin

private val attributeLabels: Map<AttributeType, String> = mapOf(
    AttributeType.PHYSIQUE to "体魄",
    AttributeType.INTELLECT to "智识",
    AttributeType.CREATION to "造物",
    AttributeType.INSIGHT to "洞察",
    AttributeType.DOMINION to "统御"
)

private val radarAttributes = listOf(
    AttributeType.PHYSIQUE,
    AttributeType.INTELLECT,
    AttributeType.CREATION,
    AttributeType.INSIGHT,
    AttributeType.DOMINION
)

@Composable
fun PanelScreen(
    viewModel: PanelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        if (uiState.isLoading) {
            Text(
                text = "…",
                style = MaterialTheme.typography.bodyLarge,
                color = InkFaint,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PanelHeader(
                    professionName = uiState.professionName,
                    totalPureTimeMin = uiState.totalPureTimeMin
                )

                RadarChartSection(
                    attributes = uiState.attributes
                )

                AffixesSection(
                    affixes = uiState.unlockedAffixes
                )
            }
        }
    }
}

@Composable
private fun PanelHeader(
    professionName: String,
    totalPureTimeMin: Int
) {
    val hours = totalPureTimeMin / 60
    val minutes = totalPureTimeMin % 60
    val timeText = "${hours}h ${minutes}m"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = professionName.ifBlank { "游侠" },
            style = MaterialTheme.typography.headlineMedium,
            color = Ink
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium,
                color = Ink
            )
            Text(
                text = "纯时",
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun RadarChartSection(
    attributes: Map<AttributeType, Int>
) {
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "属性",
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY) * 0.6f
            val attributeCount = radarAttributes.size
            val angleStep = 2f * Math.PI.toFloat() / attributeCount
            val startAngle = -Math.PI.toFloat() / 2f // start from top

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            val gridStroke = Stroke(
                width = 0.8f,
                pathEffect = dashEffect
            )
            val gridColor = InkFaint.copy(alpha = 0.5f)
            val axisColor = InkFaint.copy(alpha = 0.35f)

            // Draw 3 concentric pentagon grids
            for (ring in 1..3) {
                val ringRadius = radius * ring / 3f
                val points = mutableListOf<Offset>()
                for (i in 0 until attributeCount) {
                    val angle = startAngle + i * angleStep
                    points.add(
                        Offset(
                            centerX + ringRadius * cos(angle),
                            centerY + ringRadius * sin(angle)
                        )
                    )
                }
                for (i in points.indices) {
                    val next = points[(i + 1) % points.size]
                    drawLine(
                        color = gridColor,
                        start = points[i],
                        end = next,
                        strokeWidth = gridStroke.width,
                        pathEffect = gridStroke.pathEffect
                    )
                }
            }

            // Draw axis lines from center to each vertex
            for (i in 0 until attributeCount) {
                val angle = startAngle + i * angleStep
                val endX = centerX + radius * cos(angle)
                val endY = centerY + radius * sin(angle)
                drawLine(
                    color = axisColor,
                    start = Offset(centerX, centerY),
                    end = Offset(endX, endY),
                    strokeWidth = 0.5f,
                    pathEffect = dashEffect
                )
            }

            // Calculate max value for normalization (use at least 1 to avoid division by zero)
            val maxValue = attributes.values.maxOrNull()?.coerceAtLeast(1) ?: 1

            // Draw data polygon and data points
            val dataPoints = mutableListOf<Offset>()
            for (i in 0 until attributeCount) {
                val attrType = radarAttributes[i]
                val value = attributes[attrType] ?: 0
                val normalizedRadius = radius * (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
                val angle = startAngle + i * angleStep
                dataPoints.add(
                    Offset(
                        centerX + normalizedRadius * cos(angle),
                        centerY + normalizedRadius * sin(angle)
                    )
                )
            }

            // Draw filled data area with very subtle tint
            val dataPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(dataPoints[0].x, dataPoints[0].y)
                for (i in 1 until dataPoints.size) {
                    lineTo(dataPoints[i].x, dataPoints[i].y)
                }
                close()
            }
            drawPath(
                path = dataPath,
                color = Cinnabar.copy(alpha = 0.06f),
                style = Fill
            )
            drawPath(
                path = dataPath,
                color = Cinnabar.copy(alpha = 0.4f),
                style = Stroke(
                    width = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f), 0f)
                )
            )

            // Draw data point dots
            for (point in dataPoints) {
                drawCircle(
                    color = Cinnabar,
                    radius = 4f,
                    center = point
                )
            }

            // Draw labels (attribute name + value)
            for (i in 0 until attributeCount) {
                val attrType = radarAttributes[i]
                val label = attributeLabels[attrType] ?: ""
                val value = attributes[attrType] ?: 0
                val angle = startAngle + i * angleStep
                val labelRadius = radius + 36f
                val labelX = centerX + labelRadius * cos(angle)
                val labelY = centerY + labelRadius * sin(angle)

                val nameStyle = MaterialTheme.typography.labelMedium.copy(color = Ink)
                val valueStyle = MaterialTheme.typography.labelSmall.copy(color = InkLight)

                val nameResult = textMeasurer.measure(label, nameStyle)
                val valueResult = textMeasurer.measure("$value", valueStyle)

                val totalHeight = nameResult.size.height + valueResult.size.height + 2
                val maxWidth = maxOf(nameResult.size.width, valueResult.size.width)

                drawText(
                    textLayoutResult = nameResult,
                    color = Ink,
                    topLeft = Offset(
                        labelX - nameResult.size.width / 2f,
                        labelY - totalHeight / 2f
                    )
                )
                drawText(
                    textLayoutResult = valueResult,
                    color = InkLight,
                    topLeft = Offset(
                        labelX - valueResult.size.width / 2f,
                        labelY - totalHeight / 2f + nameResult.size.height + 2
                    )
                )
            }
        }
    }
}

@Composable
private fun AffixesSection(
    affixes: List<Affix>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "已解锁词条",
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )

        if (affixes.isEmpty()) {
            Text(
                text = "尚无解锁词条",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            affixes.forEach { affix ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Linen.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    // Small Cinnabar red stamp icon (filled red square)
                    Canvas(
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 2.dp)
                    ) {
                        drawRect(
                            color = Cinnabar,
                            size = this.size
                        )
                    }
                }
            }
        }
    }
}
