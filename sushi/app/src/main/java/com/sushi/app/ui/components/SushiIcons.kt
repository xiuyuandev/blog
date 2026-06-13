package com.sushi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.SushiMinTouchTarget
import com.sushi.app.ui.theme.SushiSpacing

/**
 * 统一图标定义 - 使用 Material Icons Outlined 风格
 */
object SushiIcons {
    val Add = Icons.Outlined.Add
    val ArrowBack = Icons.Outlined.ArrowBack
    val Calendar = Icons.Outlined.CalendarToday
    val Check = Icons.Outlined.Check
    val Close = Icons.Outlined.Close
    val Delete = Icons.Outlined.Delete
    val Edit = Icons.Outlined.Edit
    val Help = Icons.Outlined.HelpOutline
    val History = Icons.Outlined.History
    val Home = Icons.Outlined.Home
    val Info = Icons.Outlined.Info
    val Inventory = Icons.Outlined.Inventory2
    val KeyboardLeft = Icons.Outlined.KeyboardArrowLeft
    val KeyboardRight = Icons.Outlined.KeyboardArrowRight
    val KeyboardUp = Icons.Outlined.KeyboardArrowUp
    val Lock = Icons.Outlined.Lock
    val More = Icons.Outlined.MoreVert
    val Play = Icons.Outlined.PlayArrow
    val Schedule = Icons.Outlined.Schedule
    val Star = Icons.Outlined.Star
    val StarOutline = Icons.Outlined.StarOutline
    val Sync = Icons.Outlined.Sync
    val Visibility = Icons.Outlined.Visibility
    val VisibilityOff = Icons.Outlined.VisibilityOff
    val Work = Icons.Outlined.WorkOutline
}

/**
 * 统一返回按钮 - Material 3 规范
 *
 * - 48dp 圆形容器(Material 3 最小可点击区域)
 * - 24dp 图标(居中)
 * - 可选标签("返回")
 * - 颜色:surfaceVariant 底 + onSurfaceVariant 图标
 */
@Composable
fun SushiBackButton(
    onClick: () -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(
                start = SushiSpacing.xs,
                end = if (label != null) SushiSpacing.sm else 0.dp,
                top = SushiSpacing.xs,
                bottom = SushiSpacing.xs
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SushiIcons.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        if (label != null) {
            Spacer(modifier = Modifier.width(SushiSpacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 统一三点菜单按钮
 */
@Composable
fun SushiOverflowMenu(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(SushiMinTouchTarget)
        ) {
            Icon(
                imageVector = SushiIcons.More,
                contentDescription = "更多",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = if (action.isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = if (action.isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
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
 *
 * - 48dp 高(符合触屏目标)
 * - primaryContainer 底 + onPrimaryContainer 文字
 */
@Composable
fun StampDeleteButton(
    onClick: () -> Unit,
    label: String = "删除",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SushiMinTouchTarget)
            .clip(CardShapeSmall)
            .background(MaterialTheme.colorScheme.errorContainer)
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
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * 等级标签 · Material 3 Assist Chip 风格
 *
 * - Pill 形状(50% 圆角)
 * - 透明背景 12% alpha(neutral tier)或主色 12% alpha(active tier)
 * - labelSmall 字号
 */
@Composable
fun TierPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(PillShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = SushiSpacing.sm, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
