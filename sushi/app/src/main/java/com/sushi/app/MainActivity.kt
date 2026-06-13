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
import androidx.compose.ui.Modifier
import com.sushi.app.ui.navigation.SushiNavHost
import com.sushi.app.ui.theme.SushiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 Android Color 常量代替 Compose Color 的 ULong 转换
        val transparentInt = AndroidColor.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(transparentInt, transparentInt),
            navigationBarStyle = SystemBarStyle.auto(transparentInt, transparentInt)
        )
        setContent {
            val darkTheme = isSystemInDarkTheme()
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
