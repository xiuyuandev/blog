package com.sushi.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sushi.app.sync.SyncConfig
import com.sushi.app.sync.ThemeMode
import com.sushi.app.ui.navigation.SushiNavHost
import com.sushi.app.ui.theme.SushiTheme

/**
 * 素时单 Activity。
 *
 * 启动流程:
 * 1. AndroidManifest 中引用 @style/Theme.Splash(启动屏主题)
 * 2. installSplashScreen() 接管启动屏(Android 12+ 用系统 SplashScreen API,旧版本由 splashscreen 库提供)
 * 3. 启动屏结束后,自动切换到 postSplashScreenTheme(@style/Theme.Sushi)
 * 4. 主题由用户配置(跟随系统/浅色/深色),从 [com.sushi.app.sync.SyncConfigManager] 读取。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前调用,接管 Android 12+ 启动屏
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val transparentInt = AndroidColor.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(transparentInt, transparentInt),
            navigationBarStyle = SystemBarStyle.auto(transparentInt, transparentInt)
        )
        setContent {
            val themeConfig by SushiContainer.syncConfigManager.getConfigFlow()
                .collectAsState(initial = SyncConfig())
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeConfig.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SushiTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SushiNavHost()
                }
            }
        }
    }
}
