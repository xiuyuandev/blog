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
import com.sushi.app.sync.SyncConfig
import com.sushi.app.sync.ThemeMode
import com.sushi.app.ui.navigation.SushiNavHost
import com.sushi.app.ui.theme.SushiTheme

/**
 * 素时单 Activity。
 *
 * 主题由用户配置(跟随系统/浅色/深色),从 [com.sushi.app.sync.SyncConfigManager] 读取。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
