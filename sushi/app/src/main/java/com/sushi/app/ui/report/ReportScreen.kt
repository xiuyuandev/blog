package com.sushi.app.ui.report

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.ui.components.BarChart
import com.sushi.app.ui.components.HorizontalBarChart
import com.sushi.app.ui.components.SushiBackButton
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.components.SushiLoading
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.logic.WeeklyReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
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
                .padding(horizontal = SushiSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xxl)
        ) {
            Text(
                text = "周报",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
            )

            if (uiState.isLoading) {
                SushiLoading(text = "生成中")
            } else if (uiState.weeklyReport != null) {
                WeeklyReportSection(uiState.weeklyReport!!)
            }

            // 专注时段分析
            if (uiState.activePeriod.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    Text(
                        text = "24 小时专注分布",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink
                    )
                    Text(
                        text = "你最高效的时段：${uiState.peakHour}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, CardShape)
                            .clip(CardShape)
                            .background(PaperWarm)
                            .padding(16.dp)
                    ) {
                        BarChart(
                            dataPoints = uiState.activePeriod,
                            height = 140.dp
                        )
                    }
                }
            }

            // 技能占比
            if (uiState.skillDistribution.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
                    Text(
                        text = "本周技能分布",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, CardShape)
                            .clip(CardShape)
                            .background(PaperWarm)
                            .padding(16.dp)
                    ) {
                        HorizontalBarChart(
                            dataPoints = uiState.skillDistribution.entries
                                .sortedByDescending { it.value }
                                .take(8)
                                .map { it.key to it.value }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
        }
    }
}

@Composable
private fun WeeklyReportSection(report: WeeklyReport) {
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    val weekRange = "${dateFormat.format(Date(report.weekStart))} - ${dateFormat.format(Date(report.weekEnd - 1))}"
    val totalHours = report.totalMin / 60
    val totalMinutes = report.totalMin % 60
    val lastWeekHours = report.lastWeekMin / 60
    val diffPercent = if (report.lastWeekMin > 0) ((report.totalMin - report.lastWeekMin).toFloat() / report.lastWeekMin * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg)
    ) {
        Text(
            text = weekRange,
            style = MaterialTheme.typography.labelLarge,
            color = InkFaint
        )

        // 总时长
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "本周总纯时间",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint
            )
            Text(
                text = if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Cinnabar
            )
            Text(
                text = if (diffPercent > 0) "比上周 +${diffPercent}%" else if (diffPercent < 0) "比上周 ${diffPercent}%" else "与上周持平",
                style = MaterialTheme.typography.bodySmall,
                color = if (diffPercent >= 0) Cinnabar else InkFaint
            )
        }

        // 数据点
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataPoint(label = "专注次数", value = "${report.recordCount}")
            DataPoint(label = "最高产技能", value = report.topSkillName)
            DataPoint(label = "平均中断", value = "%.1f".format(report.avgInterruptsPerSession))
        }

        // 上周对比
        Text(
            text = "上周累计：${lastWeekHours}h ${report.lastWeekMin % 60}m",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint
        )
    }
}

@Composable
private fun DataPoint(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Ink
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InkFaint
        )
    }
}
