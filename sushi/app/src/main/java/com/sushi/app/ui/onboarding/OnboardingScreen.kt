package com.sushi.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sushi.app.ui.components.SushiIcons
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.MaterialColor
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiMinTouchTarget
import com.sushi.app.ui.theme.SushiSpacing

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentPage by rememberSaveable { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "素时",
            subtitle = "柳比歇夫的时间美学",
            description = "不被算法绑架，不被数字裹挟。\n用纯时间丈量生命，让每一刻专注都被铭记。",
            icon = SushiIcons.Schedule
        ),
        OnboardingPage(
            title = "经验流转",
            subtitle = "1 分钟纯时间 = 1 点经验",
            description = "任务绑定技能，结算纯时间自动注入经验。\n技能等级自动贡献到分类主属性，\n每 120 点经验提升一个等级。",
            icon = SushiIcons.Star
        ),
        OnboardingPage(
            title = "四级材质",
            subtitle = "原石 → 青铜 → 赤金 → 黑曜石",
            description = "等级越高，视觉冲击越强。\nLV 100 以上的技能可以主动「毕业」。\n从游侠到剑客，身份的演化就在每一次专注。",
            icon = SushiIcons.Star
        ),
        OnboardingPage(
            title = "诚实记录",
            subtitle = "扣除杂质时间",
            description = "专注 1 小时，被打断 3 次，\n可能只有 40 分钟是纯时间。\n这种诚实是你理解时间的关键。",
            icon = SushiIcons.History
        ),
        OnboardingPage(
            title = "开始吧",
            subtitle = "现在去建你的第一个技能",
            description = "你可以随时回到这里查看更多帮助。\n准备好后，点击「开始」进入主界面。",
            icon = SushiIcons.Star
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialColor.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SushiSpacing.xxxl, vertical = SushiSpacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            val page = pages[currentPage]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SushiSpacing.lg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialColor.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = MaterialColor.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialColor.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialColor.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(SushiSpacing.md))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialColor.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(SushiSpacing.xxxl))

            // 页面指示 - Material 3:12dp 当前页,6dp 其余
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (i == currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) MaterialColor.primary else MaterialColor.outlineVariant)
                    )
                    if (i < pages.lastIndex) Spacer(modifier = Modifier.size(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(SushiSpacing.xxl))

            // 按钮 - Material 3 触屏目标 ≥ 48dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    Button(
                        onClick = { currentPage-- },
                        shape = CardShape,
                        modifier = Modifier.defaultMinSize(
                            minWidth = SushiMinTouchTarget,
                            minHeight = SushiMinTouchTarget
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialColor.surfaceVariant,
                            contentColor = MaterialColor.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "上一步",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            viewModel.markOnboarded()
                            onCompleted()
                        }
                    },
                    shape = CardShape,
                    modifier = Modifier.defaultMinSize(
                        minWidth = SushiMinTouchTarget,
                        minHeight = SushiMinTouchTarget
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialColor.primary,
                        contentColor = MaterialColor.onPrimary
                    )
                ) {
                    Text(
                        text = if (currentPage == pages.lastIndex) "开始" else "下一步",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
