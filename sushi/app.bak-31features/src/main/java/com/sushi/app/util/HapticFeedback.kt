package com.sushi.app.util

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * 触觉反馈 - 关键事件调用
 * @param type LONG_PRESS 升级/解锁, CONFIRM 结算, KEYBOARD_TAP 普通按钮
 */
enum class HapticType {
    LONG_PRESS,    // 重大事件
    CONFIRM,       // 确认操作
    KEYBOARD_TAP,  // 普通点击
    REJECT         // 错误/失败
}

@Composable
fun rememberHaptic(): (HapticType) -> Unit {
    val view: View = LocalView.current
    return remember(view) {
        { type: HapticType ->
            view.performHapticFeedback(
                when (type) {
                    HapticType.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
                    HapticType.CONFIRM -> HapticFeedbackConstants.CONFIRM
                    HapticType.KEYBOARD_TAP -> HapticFeedbackConstants.KEYBOARD_TAP
                    HapticType.REJECT -> HapticFeedbackConstants.REJECT
                },
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
    }
}
