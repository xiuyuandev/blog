package com.sushi.app.ui.navigation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sushi.app.sync.SyncConfigManager
import com.sushi.app.ui.achievement.AchievementScreen
import com.sushi.app.ui.focus.FocusScreen
import com.sushi.app.ui.goal.GoalScreen
import com.sushi.app.ui.help.HelpScreen
import com.sushi.app.ui.onboarding.OnboardingScreen
import com.sushi.app.ui.panel.PanelScreen
import com.sushi.app.ui.profession.ProfessionScreen
import com.sushi.app.ui.reflection.ReflectionScreen
import com.sushi.app.ui.report.ReportScreen
import com.sushi.app.ui.review.ReviewScreen
import com.sushi.app.ui.skill.SkillScreen
import com.sushi.app.ui.skill.tree.SkillTreeScreen
import com.sushi.app.ui.sync.SyncScreen
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.SushiSpacing

sealed class SushiRoute(val route: String, val label: String, val icon: ImageVector) {
    data object Panel : SushiRoute("panel", "我", com.sushi.app.ui.components.SushiIcons.Home)
    data object Focus : SushiRoute("focus", "专注", com.sushi.app.ui.components.SushiIcons.Play)
    data object Skill : SushiRoute("skill", "技能", com.sushi.app.ui.components.SushiIcons.Star)
    data object Review : SushiRoute("review", "复盘", com.sushi.app.ui.components.SushiIcons.History)
    data object Profession : SushiRoute("profession", "职业", com.sushi.app.ui.components.SushiIcons.Work)

    data object Sync : SushiRoute("sync", "同步", com.sushi.app.ui.components.SushiIcons.Sync)
    data object Achievement : SushiRoute("achievement", "成就", com.sushi.app.ui.components.SushiIcons.Star)
    data object Goal : SushiRoute("goal", "目标", com.sushi.app.ui.components.SushiIcons.Star)
    data object Report : SushiRoute("report", "周报", com.sushi.app.ui.components.SushiIcons.Calendar)
    data object Reflection : SushiRoute("reflection", "反思", com.sushi.app.ui.components.SushiIcons.Edit)
    data object Onboarding : SushiRoute("onboarding", "引导", com.sushi.app.ui.components.SushiIcons.Star)
    data object Help : SushiRoute("help", "帮助", com.sushi.app.ui.components.SushiIcons.Help)
    data object SkillTree : SushiRoute("skill_tree", "技能树", com.sushi.app.ui.components.SushiIcons.Star)
}

private val bottomBarRoutes = setOf(
    SushiRoute.Panel.route,
    SushiRoute.Focus.route,
    SushiRoute.Skill.route,
    SushiRoute.Review.route,
    SushiRoute.Profession.route
)

@Composable
fun SushiNavHost(
    navController: NavHostController = rememberNavController(),
    syncConfigManager: SyncConfigManager? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        NavHost(
            navController = navController,
            startDestination = SushiRoute.Panel.route
        ) {
            composable(SushiRoute.Panel.route) {
                PanelScreen(
                    onNavigateToSync = { navController.navigate(SushiRoute.Sync.route) },
                    onNavigateToAchievement = { navController.navigate(SushiRoute.Achievement.route) },
                    onNavigateToGoal = { navController.navigate(SushiRoute.Goal.route) },
                    onNavigateToReport = { navController.navigate(SushiRoute.Report.route) },
                    onNavigateToReflection = { navController.navigate(SushiRoute.Reflection.route) },
                    onNavigateToHelp = { navController.navigate(SushiRoute.Help.route) }
                )
            }
            composable(SushiRoute.Focus.route) { FocusScreen() }
            composable(SushiRoute.Skill.route) {
                SkillScreen(
                    onNavigateToSkillTree = { navController.navigate(SushiRoute.SkillTree.route) }
                )
            }
            composable(SushiRoute.Review.route) {
                ReviewScreen(
                    onNavigateToReport = { navController.navigate(SushiRoute.Report.route) }
                )
            }
            composable(SushiRoute.Profession.route) { ProfessionScreen() }
            composable(SushiRoute.Sync.route) {
                SyncScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Achievement.route) {
                AchievementScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Goal.route) {
                GoalScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Report.route) {
                ReportScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Reflection.route) {
                ReflectionScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Help.route) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable(SushiRoute.Onboarding.route) {
                OnboardingScreen(onCompleted = { navController.popBackStack() })
            }
            composable(SushiRoute.SkillTree.route) {
                SkillTreeScreen(
                    onBack = { navController.popBackStack() },
                    onSkillClick = { /* 暂时不跳转详情页 */ }
                )
            }
        }

        if (showBottomBar) {
            SushiBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(SushiRoute.Panel.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun SushiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        SushiRoute.Panel,
        SushiRoute.Focus,
        SushiRoute.Skill,
        SushiRoute.Review,
        SushiRoute.Profession
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(vertical = SushiSpacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            Column(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(if (selected) CinnabarFaint else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) Cinnabar else InkLight,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Cinnabar else InkLight
                )
            }
        }
    }
}
