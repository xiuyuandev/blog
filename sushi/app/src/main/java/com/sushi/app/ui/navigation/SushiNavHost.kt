package com.sushi.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sushi.app.ui.panel.PanelScreen
import com.sushi.app.ui.focus.FocusScreen
import com.sushi.app.ui.skill.SkillScreen
import com.sushi.app.ui.profession.ProfessionScreen
import com.sushi.app.ui.review.ReviewScreen
import com.sushi.app.ui.sync.SyncScreen
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.Paper

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Panel : Screen("panel", "面板", Icons.Outlined.Person)
    data object Focus : Screen("focus", "专注", Icons.Outlined.HourglassTop)
    data object Skill : Screen("skill", "技能", Icons.Outlined.AutoStories)
    data object Profession : Screen("profession", "职业", Icons.Outlined.Badge)
    data object Review : Screen("review", "复盘", Icons.Outlined.CalendarMonth)
    data object Sync : Screen("sync", "同步", Icons.Outlined.CloudSync)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SushiNavHost() {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Panel,
        Screen.Focus,
        Screen.Skill,
        Screen.Profession,
        Screen.Review
    )

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            // 同步页面不显示底部导航
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Screen.Sync.route) {
                NavigationBar(
                    containerColor = Paper,
                    contentColor = Ink
                ) {
                    val navEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navEntry?.destination

                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        Ink else InkFaint
                                )
                            },
                            label = {
                                Text(
                                    screen.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        Ink else InkFaint
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Paper
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Panel.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Panel.route) {
                PanelScreen(onNavigateToSync = { navController.navigate(Screen.Sync.route) })
            }
            composable(Screen.Focus.route) { FocusScreen() }
            composable(Screen.Skill.route) { SkillScreen() }
            composable(Screen.Profession.route) { ProfessionScreen() }
            composable(Screen.Review.route) { ReviewScreen() }
            composable(Screen.Sync.route) {
                SyncScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
