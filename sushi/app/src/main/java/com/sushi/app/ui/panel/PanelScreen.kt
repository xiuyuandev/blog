package com.sushi.app.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.sushi.app.data.model.Affix
import com.sushi.app.data.model.AttributeType
import com.sushi.app.data.model.Profession
import com.sushi.app.ui.components.SushiDivider
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.viewmodel.PanelViewModel
import java.text.NumberFormat
import kotlin.math.cos
import kotlin.math.hypot
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
    viewModel: PanelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToSync: () -> Unit = {},
    onNavigateToAchievement: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToReflection: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialColor.background)
    ) {
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SushiLoading(text = "加载中")
        }

        AnimatedVisibility(
            visible = !uiState.isLoading,
            enter = fadeIn(animationSpec = tween(SushiAnim.NORMAL)),
            exit = fadeOut()
        ) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xl)
        ) {
                PanelHeader(
                    professionName = uiState.professionName,
                    totalPureTimeMin = uiState.totalPureTimeMin,
                    allProfessions = uiState.allProfessions,
                    onProfessionSelected = { viewModel.selectProfession(it) }
                )

                RadarChartSection(
                    attributes = uiState.attributes
                )

                SectionDivider()

                AffixesSection(
                    affixes = uiState.unlockedAffixes
                )

                Spacer(modifier = Modifier.height(SushiSpacing.sm))

                // 快捷入口区
                QuickActionsGrid(
                    onAchievement = onNavigateToAchievement,
                    onGoal = onNavigateToGoal,
                    onReport = onNavigateToReport,
                    onReflection = onNavigateToReflection,
                    onHelp = onNavigateToHelp
                )

                Spacer(modifier = Modifier.height(SushiSpacing.sm))

                SyncEntry(onClick = onNavigateToSync)
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAchievement: () -> Unit,
    onGoal: () -> Unit,
    onReport: () -> Unit,
    onReflection: () -> Unit,
    onHelp: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
        Text(
            text = "快捷入口",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialColor.onSurfaceVariant
        )
        // 第一行
        Row(horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
            QuickActionTile(label = "成就", icon = SushiIcons.Star, onClick = onAchievement, modifier = Modifier.weight(1f))
            QuickActionTile(label = "目标", icon = SushiIcons.Edit, onClick = onGoal, modifier = Modifier.weight(1f))
            QuickActionTile(label = "周报", icon = SushiIcons.Calendar, onClick = onReport, modifier = Modifier.weight(1f))
        }
        // 第二行
        Row(horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
            QuickActionTile(label = "反思", icon = SushiIcons.History, onClick = onReflection, modifier = Modifier.weight(1f))
            QuickActionTile(label = "帮助", icon = SushiIcons.Help, onClick = onHelp, modifier = Modifier.weight(1f))
            QuickActionTile(label = "同步", icon = SushiIcons.Sync, onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 快捷入口瓦片 · Material 3 规范
 *
 * - 高度 80dp(48dp 触屏目标 + 余量)
 * - 背景:surfaceVariant
 * - 选中/激活态:background = primaryContainer
 * - 24dp 图标(主操作) + labelMedium 文字
 */
@Composable
private fun QuickActionTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(80.dp)
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = SushiSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialColor.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialColor.onSurface
        )
    }
}

@Composable
private fun PanelHeader(
    professionName: String,
    totalPureTimeMin: Int,
    allProfessions: List<Profession>,
    onProfessionSelected: (String) -> Unit
) {
    val hours = totalPureTimeMin / 60
    val minutes = totalPureTimeMin % 60
    val timeText = "${hours}h ${minutes}m"

    val formattedExp = NumberFormat.getNumberInstance().format(totalPureTimeMin)

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
    ) {
        // 节气 + 农历 + 今日日期(Week 3 新增: 中国本土化)
        DateSubHeader()

        Box {
            Text(
                text = professionName.ifBlank { "游侠" },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialColor.onSurface,
                modifier = Modifier.clickable { dropdownExpanded = true }
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                allProfessions.forEach { profession ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = profession.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (profession.name == professionName) MaterialColor.primary else MaterialColor.onSurface
                            )
                        },
                        onClick = {
                            onProfessionSelected(profession.id)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialColor.onSurface
            )
            Text(
                text = "纯时",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.onSurfaceVariant,
                modifier = Modifier.padding(bottom = SushiSpacing.xs)
            )
        }
        Text(
            text = "$formattedExp Exp",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialColor.outline
        )

        // 今日古语(Week 3 新增: 节气古语)
        QuoteSubHeader()
    }
}

/**
 * 顶部日期/节气/农历子标题(中国本土化)
 *
 * 显示: 2026年6月13日 周六 · 芒种 · 五月廿八
 */
@Composable
private fun DateSubHeader() {
    val today = remember { java.util.Date() }
    val solarTerm = remember { com.sushi.app.util.SolarTerm.atDate(today) }
    val lunar = remember { com.sushi.app.util.LunarConverter().solarToLunar(today) }

    val dateFormat = remember { java.text.SimpleDateFormat("yyyy年M月d日 EEE", java.util.Locale.CHINA) }
    val dateText = dateFormat.format(today)
    val termText = solarTerm?.displayName?.let { "· $it" } ?: ""
    val lunarText = "· ${lunar.yearGanZhi}${lunar.yearZodiac}年 ${lunar.display()}"

    Text(
        text = "$dateText $termText $lunarText",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialColor.onSurfaceVariant
    )
}

/**
 * 今日古语(从 24 节气古语库中随机抽取)
 */
@Composable
private fun QuoteSubHeader() {
    val today = remember { java.util.Date() }
    val quote = remember { com.sushi.app.util.SolarTermQuotes.randomForToday(today) }
    if (quote.isNullOrBlank()) return

    Text(
        text = "「$quote」",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialColor.outline,
        modifier = Modifier.padding(top = SushiSpacing.xs)
    )
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(SushiSpacing.sm))
    SushiDivider()
    Spacer(modifier = Modifier.height(SushiSpacing.sm))
}

@Composable
private fun RadarChartSection(
    attributes: Map<AttributeType, Int>
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedAttribute by remember { mutableStateOf<AttributeType?>(null) }

    // Animate attribute values
    val animatedPhysique by animateIntAsState(
        targetValue = attributes[AttributeType.PHYSIQUE] ?: 0,
        animationSpec = tween(SushiAnim.NORMAL), label = "physique"
    )
    val animatedIntellect by animateIntAsState(
        targetValue = attributes[AttributeType.INTELLECT] ?: 0,
        animationSpec = tween(SushiAnim.NORMAL), label = "intellect"
    )
    val animatedCreation by animateIntAsState(
        targetValue = attributes[AttributeType.CREATION] ?: 0,
        animationSpec = tween(SushiAnim.NORMAL), label = "creation"
    )
    val animatedInsight by animateIntAsState(
        targetValue = attributes[AttributeType.INSIGHT] ?: 0,
        animationSpec = tween(SushiAnim.NORMAL), label = "insight"
    )
    val animatedDominion by animateIntAsState(
        targetValue = attributes[AttributeType.DOMINION] ?: 0,
        animationSpec = tween(SushiAnim.NORMAL), label = "dominion"
    )

    val animatedAttributes = mapOf(
        AttributeType.PHYSIQUE to animatedPhysique,
        AttributeType.INTELLECT to animatedIntellect,
        AttributeType.CREATION to animatedCreation,
        AttributeType.INSIGHT to animatedInsight,
        AttributeType.DOMINION to animatedDominion
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        Text(
            text = "属性",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialColor.onSurfaceVariant
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cx = w / 2f
                        val cy = h / 2f
                        val radius = minOf(cx, cy) * 0.6f
                        val attributeCount = radarAttributes.size
                        val angleStep = 2f * Math.PI.toFloat() / attributeCount
                        val startAngle = -Math.PI.toFloat() / 2f

                        var nearest: AttributeType? = null
                        var minDist = Float.MAX_VALUE
                        for (i in 0 until attributeCount) {
                            val angle = startAngle + i * angleStep
                            val px = cx + radius * cos(angle)
                            val py = cy + radius * sin(angle)
                            val d = hypot(offset.x - px, offset.y - py)
                            if (d < minDist) {
                                minDist = d
                                nearest = radarAttributes[i]
                            }
                        }
                        // 选中 48dp 半径内的属性(Material 3 触屏目标)
                        if (minDist < 96f && nearest != null) {
                            selectedAttribute = if (selectedAttribute == nearest) null else nearest
                        } else {
                            selectedAttribute = null
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY) * 0.6f
            val attributeCount = radarAttributes.size
            val angleStep = 2f * Math.PI.toFloat() / attributeCount
            val startAngle = -Math.PI.toFloat() / 2f

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            val gridStroke = Stroke(
                width = 0.8f,
                pathEffect = dashEffect
            )
            val gridColor = MaterialColor.outline.copy(alpha = 0.5f)
            val axisColor = MaterialColor.outline.copy(alpha = 0.35f)

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
            val maxValue = animatedAttributes.values.maxOrNull()?.coerceAtLeast(1) ?: 1

            // Draw data polygon and data points
            val dataPoints = mutableListOf<Offset>()
            for (i in 0 until attributeCount) {
                val attrType = radarAttributes[i]
                val value = animatedAttributes[attrType] ?: 0
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
            val dataPath = Path().apply {
                moveTo(dataPoints[0].x, dataPoints[0].y)
                for (i in 1 until dataPoints.size) {
                    lineTo(dataPoints[i].x, dataPoints[i].y)
                }
                close()
            }
            drawPath(
                path = dataPath,
                color = MaterialColor.primary.copy(alpha = 0.06f),
                style = Fill
            )
            drawPath(
                path = dataPath,
                color = MaterialColor.primary.copy(alpha = 0.4f),
                style = Stroke(
                    width = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 3f), 0f)
                )
            )

            // Draw data point dots
            for (point in dataPoints) {
                drawCircle(
                    color = MaterialColor.primary,
                    radius = 4f,
                    center = point
                )
            }

            // Draw labels (attribute name + value)
            for (i in 0 until attributeCount) {
                val attrType = radarAttributes[i]
                val label = attributeLabels[attrType] ?: ""
                val value = animatedAttributes[attrType] ?: 0
                val angle = startAngle + i * angleStep
                val labelRadius = radius + 36f
                val labelX = centerX + labelRadius * cos(angle)
                val labelY = centerY + labelRadius * sin(angle)

                val isHighlighted = selectedAttribute == attrType
                val nameStyle = MaterialTheme.typography.labelMedium.copy(
                    color = if (isHighlighted) MaterialColor.primary else MaterialColor.onSurface
                )
                val valueStyle = MaterialTheme.typography.labelSmall.copy(
                    color = if (isHighlighted) MaterialColor.primary else MaterialColor.onSurfaceVariant
                )

                val nameResult = textMeasurer.measure(label, nameStyle)
                val valueResult = textMeasurer.measure("$value", valueStyle)

                val totalHeight = nameResult.size.height + valueResult.size.height + 2

                drawText(
                    textLayoutResult = nameResult,
                    color = if (isHighlighted) MaterialColor.primary else MaterialColor.onSurface,
                    topLeft = Offset(
                        labelX - nameResult.size.width / 2f,
                        labelY - totalHeight / 2f
                    )
                )
                drawText(
                    textLayoutResult = valueResult,
                    color = if (isHighlighted) MaterialColor.primary else MaterialColor.onSurfaceVariant,
                    topLeft = Offset(
                        labelX - valueResult.size.width / 2f,
                        labelY - totalHeight / 2f + nameResult.size.height + 2
                    )
                )

                if (isHighlighted) {
                    // 高亮选中：在该属性顶点画圆
                    val vx = centerX + radius * cos(angle)
                    val vy = centerY + radius * sin(angle)
                    drawCircle(
                        color = MaterialColor.primaryContainer,
                        radius = 16f,
                        center = Offset(vx, vy)
                    )
                }
            }
        }

        // 选中属性的详情条
        AnimatedVisibility(
            visible = selectedAttribute != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val attr = selectedAttribute
            if (attr != null) {
                val value = animatedAttributes[attr] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(MaterialColor.primaryContainer)
                        .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = attributeLabels[attr] ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialColor.primary
                    )
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialColor.primary
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
    ) {
        Text(
            text = "已解锁词条",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialColor.onSurfaceVariant
        )

        if (affixes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, CardShape)
                    .clip(CardShape)
                    .background(MaterialColor.surfaceVariant)
                    .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
            ) {
                Icon(
                    imageVector = SushiIcons.Lock,
                    contentDescription = null,
                    tint = MaterialColor.outline,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "尚无解锁词条",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialColor.outline
                )
            }
        } else {
            affixes.forEach { affix ->
                AffixRow(affix = affix)
            }
        }
    }
}

@Composable
private fun AffixRow(affix: Affix) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surfaceVariant)
            .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.md),
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
                color = MaterialColor.onSurface
            )
            Text(
                text = affix.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialColor.onSurfaceVariant
            )
        }
        // 朱砂红印章标识
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CardShape)
                .background(MaterialColor.primary)
        )
    }
}

@Composable
private fun SyncEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialColor.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md)
        ) {
            Icon(
                imageVector = SushiIcons.Sync,
                contentDescription = null,
                tint = MaterialColor.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "同步与备份",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialColor.onSurface
            )
        }
        Icon(
            imageVector = SushiIcons.KeyboardRight,
            contentDescription = null,
            tint = MaterialColor.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
