package com.sushi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.SushiSpacing

/**
 * 统一加载状态 - 朱砂红细环
 */
@Composable
fun SushiLoading(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Cinnabar,
                strokeWidth = 2.5.dp
            )
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint
                )
            }
        }
    }
}
