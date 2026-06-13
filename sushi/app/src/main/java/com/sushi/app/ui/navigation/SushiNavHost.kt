package com.sushi.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.sushi.app.ui.theme.MaterialColor

/**
 * 5 Tab 路由定义
 *
 * V1.0 5 Tab 导航:
 * - 主页(Panel): 总览、连续记录、今日要点
 * - 专注(Focus): 计时 + 技能选择
 * - 技能(Skill): 技能列表 + 详情 + 树
 * - 复盘(Review): 当日复盘 + 历史
 * - 我的(Profile): 设置 + 同步 + 帮助 + 关于
 */
sealed class SushiRoute(val route: String, val label: String, val icon: ImageVector) {
    data object Panel : SushiRoute("panel", "主页", Icons.Outlined.Home)
    data object Focus : SushiRoute("focus", "专注", Icons.Outlined.PlayCircle)
    data object Skill : SushiRoute("skill", "技能", Icons.Outlined.Star)
    data object Review : SushiRoute("review", "复盘", Icons.Outlined.History)
    data object Profile : SushiRoute("profession", "我的", Icons.Outlined.Person)

    // 二级页面
    data object Sync : SushiRoute("sync", "同步", Icons.Outlined.Person)
    data object Achievement : SushiRoute("achievement", "成就", Icons.Outlined.Star)
    data object Goal : SushiRoute("goal", "目标", Icons.Outlined.Star)
    data object Report : SushiRoute("report", "报告", Icons.Outlined.History)
    data object Reflection : SushiRoute("reflection", "反思", Icons.Outlined.History)
    data object Onboarding : SushiRoute("onboarding", "引导", Icons.Outlined.Person)
    data object Help : SushiRoute("help", "帮助", Icons.Outlined.Person)
    data object SkillTree : SushiRoute("skill_tree", "技能树", Icons.Outlined.Star)
}

/** 5 Tab 路由(决定是否显示底部导航) */
private val bottomBarRoutes = setOf(
    SushiRoute.Panel.route,
    SushiRoute.Focus.route,
    SushiRoute.Skill.route,
    SushiRoute.Review.route,
    SushiRoute.Profile.route
)

/** 5 Tab 项(顺序即为显示顺序) */
private val tabItems = listOf(
    SushiRoute.Panel,
    SushiRoute.Focus,
    SushiRoute.Skill,
    SushiRoute.Review,
    SushiRoute.Profile
)

@Composable
fun SushiNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = SushiRoute.Panel.route,
            modifier = Modifier.fillMaxSize()
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
            composable(SushiRoute.Profile.route) { ProfessionScreen() }
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

        // Material 3 底部导航(仅主 Tab 显示)
        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        ) {
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
                }
            )
        }
    }
}

/**
 * Material 3 底部导航
 *
 * - tonalElevation 0(Material 3 默认)
 * - containerColor: surface(无色调,纯色)
 * - indicatorColor: secondaryContainer(选中态)
 * - selectedTextColor: onSecondaryContainer
 */
@Composable
private fun SushiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialColor.surface,
        contentColor = MaterialColor.onSurface,
        tonalElevation = 0.dp
    ) {
        tabItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialColor.onSecondaryContainer,
                    selectedTextColor = MaterialColor.onSurface,
                    unselectedIconColor = MaterialColor.onSurfaceVariant,
                    unselectedTextColor = MaterialColor.onSurfaceVariant,
                    indicatorColor = MaterialColor.secondaryContainer
                )
            )
        }
    }
}
