package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.SushiSpacing

/**
 * 统一图标定义 - 消除 Text 字符图标
 */
object SushiIcons {
    val Add = Icons.Outlined.Add
    val ArrowBack = Icons.Outlined.ArrowBack
    val Calendar = Icons.Outlined.CalendarToday
    val Check = Icons.Outlined.Check
    val Close = Icons.Outlined.Close
    val Delete = Icons.Outlined.Delete
    val Edit = Icons.Outlined.Edit
    val ExpandLess = Icons.Outlined.ExpandLess
    val ExpandMore = Icons.Outlined.ExpandMore
    val Help = Icons.Outlined.HelpOutline
    val History = Icons.Outlined.History
    val Home = Icons.Outlined.Home
    val Info = Icons.Outlined.Info
    val Inventory = Icons.Outlined.Inventory2
    val KeyboardDown = Icons.Outlined.KeyboardArrowDown
    val KeyboardLeft = Icons.Outlined.KeyboardArrowLeft
    val KeyboardRight = Icons.Outlined.KeyboardArrowRight
    val KeyboardUp = Icons.Outlined.KeyboardArrowUp
    val Lock = Icons.Outlined.Lock
    val More = Icons.Outlined.MoreVert
    val Play = Icons.Outlined.PlayArrow
    val Schedule = Icons.Outlined.Schedule
    val Search = Icons.Outlined.Search
    val Settings = Icons.Outlined.Settings
    val Star = Icons.Outlined.Star
    val StarOutline = Icons.Outlined.StarOutline
    val Sync = Icons.Outlined.Sync
    val Visibility = Icons.Outlined.Visibility
    val VisibilityOff = Icons.Outlined.VisibilityOff
    val Work = Icons.Outlined.WorkOutline
}

/**
 * 统一返回按钮 - 圆形 Linen 底
 */
@Composable
fun SushiBackButton(
    onClick: () -> Unit,
    label: String = "返回"
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = SushiSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(com.sushi.app.ui.theme.Linen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SushiIcons.ArrowBack,
                contentDescription = null,
                tint = InkLight,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(SushiSpacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkLight
        )
    }
}

/**
 * 统一三点菜单按钮 - 危险操作入口
 */
@Composable
fun SushiOverflowMenu(
    actions: List<MenuAction>
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = SushiIcons.More,
                contentDescription = "更多",
                tint = InkLight
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = action.label,
                            color = if (action.isDestructive) Cinnabar else com.sushi.app.ui.theme.Ink
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = if (action.isDestructive) Cinnabar else InkLight
                        )
                    },
                    onClick = {
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}

data class MenuAction(
    val label: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 印章风格删除按钮
 */
@Composable
fun StampDeleteButton(
    onClick: () -> Unit,
    label: String = "删除技能"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CinnabarFaint)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Icon(
                imageVector = SushiIcons.Delete,
                contentDescription = null,
                tint = Cinnabar,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = Cinnabar,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
