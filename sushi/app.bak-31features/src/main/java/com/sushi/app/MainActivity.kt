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
import com.sushi.app.sync.SyncConfigManager
import com.sushi.app.sync.ThemeMode
import com.sushi.app.ui.navigation.SushiNavHost
import com.sushi.app.ui.theme.SushiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var configManager: SyncConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparentInt = AndroidColor.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(transparentInt, transparentInt),
            navigationBarStyle = SystemBarStyle.auto(transparentInt, transparentInt)
        )
        setContent {
            val themeConfig by configManager.getConfigFlow().collectAsState(initial = com.sushi.app.sync.SyncConfig())
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
