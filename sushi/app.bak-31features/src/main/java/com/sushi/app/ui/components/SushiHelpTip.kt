package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper

/**
 * 概念解释卡片 - 悬浮提示
 */
data class ConceptInfo(
    val title: String,
    val description: String
)

@Composable
fun ConceptExplainerCard(
    info: ConceptInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CinnabarFaint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cinnabar,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper
                )
            ) {
                Text("明白了", fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Paper
    )
}

/**
 * 内联概念提示 - 出现在标签旁的"?"图标
 */
@Composable
fun InlineConceptTip(
    info: ConceptInfo,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Icon(
        imageVector = SushiIcons.Help,
        contentDescription = "什么是${info.title}?",
        tint = InkFaint,
        modifier = modifier
            .size(14.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(Linen)
            .clickable { showDialog = true }
    )

    if (showDialog) {
        ConceptExplainerCard(info = info, onDismiss = { showDialog = false })
    }
}
