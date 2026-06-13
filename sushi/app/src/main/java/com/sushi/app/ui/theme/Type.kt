package com.sushi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 素时字体系统 · Material 3 Type Scale 规范
 *
 * Material 3 完整 Type Scale 包含 15 个 token(display L/M/S, headline L/M/S,
 * title L/M/S, body L/M/S, label L/M/S),每个都有规定的 size/lineHeight/letterSpacing。
 *
 * - display: 品牌字号,用于最大标题(主屏纯时间)
 * - headline: 屏幕级标题(技能名、技能等级)
 * - title: 卡片级标题
 * - body: 正文
 * - label: 按钮、标签、辅助信息
 *
 * 字距:Material 3 规范
 * - display/headline: 0(中文)
 * - title: 0
 * - body L: 0.5sp / M: 0.25sp / S: 0.4sp
 * - label: 0.5sp
 */

// 衬线字体(大标题/等级/纯时间数字)
val SerifFontFamily = FontFamily.Serif

// 无衬线字体(正文)
val SansFontFamily = FontFamily.SansSerif

val SushiTypography = Typography(
    // ============ Display(品牌标题) ============
    // Material 3 spec: 57sp / 64sp / 0
    displayLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    // 45 / 52
    displayMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    // 36 / 44
    displaySmall = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // ============ Headline(屏幕标题) ============
    // 32 / 40
    headlineLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // 28 / 36
    headlineMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // 24 / 32
    headlineSmall = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // ============ Title(卡片标题) ============
    // 22 / 28
    titleLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // 16 / 24, letterSpacing 0.15
    titleMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // 14 / 20, letterSpacing 0.1
    titleSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ============ Body(正文) ============
    // 16 / 24, letterSpacing 0.5
    bodyLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 14 / 20, letterSpacing 0.25
    bodyMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // 12 / 16, letterSpacing 0.4
    bodySmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ============ Label(按钮、辅助) ============
    // 14 / 20, letterSpacing 0.1
    labelLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // 12 / 16, letterSpacing 0.5
    labelMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // 11 / 16, letterSpacing 0.5
    labelSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
