package com.daycountapp.ui.theme

import androidx.compose.ui.graphics.Color

// Apple Design System Colors - Light Mode
val PageBackground = Color(0xFFFFFFFF)
val CardBackground = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1D1D1F)
val TextSecondary = Color(0xFF86868B)
val TextTertiary = Color(0xFFAEAEB2)
val BrandBlue = Color(0xFF007AFF)
val BrandBlueDark = Color(0xFF0A84FF)
val Separator = Color(0xFFD2D2D7)
val SearchBackground = Color(0x1C1C1C1F)
val DestructiveRed = Color(0xFFFF3B30)
val SuccessGreen = Color(0xFF34C759)
val TabBarBackground = Color(0xF2FFFFFF)
val NavBarBackground = Color(0xCCFFFFFF)

// Apple Design System Colors - Dark Mode
val DarkPageBackground = Color(0xFF000000)
val DarkCardBackground = Color(0xFF2C2C2E)
val DarkTextPrimary = Color(0xFFF5F5F7)
val DarkTextSecondary = Color(0xFF98989D)
val DarkSeparator = Color(0xFF38383A)
val DarkSearchBackground = Color(0x3C1C1C1F)
val DarkTabBarBackground = Color(0xF21C1C1E)
val DarkNavBarBackground = Color(0xCC1C1C1E)

// Event Color Presets — 14 种预设纯色
val EventColors =
    listOf(
        BrandBlue, // 0  蓝色
        Color(0xFF34C759), // 1  绿色
        Color(0xFFFF9500), // 2  橙色
        Color(0xFFFF2D55), // 3  粉色
        Color(0xFF5856D6), // 4  紫色
        Color(0xFF00C7BE), // 5  青色
        Color(0xFFFF3B30), // 6  红色
        Color(0xFFAF52DE), // 7  紫红
        Color(0xFFDDA0DD), // 8  梅色
        Color(0xFFF0E68C), // 9  卡其
        Color(0xFFFFA07A), // 10 浅鲑
        Color(0xFF98D8C8), // 11 薄荷
        Color(0xFFB0C4DE), // 12 钢蓝
        Color(0xFFFFD700), // 13 金色
    )

// 特殊索引标记
const val CUSTOM_COLOR_INDEX = 14 // 自定义颜色
const val GRADIENT_INDEX = 15 // 过渡色模式

// 渐变图标默认颜色（红色→浅黄色，对比明显）
val GradientIconStart = Color(0xFFFF3B30) // 红
val GradientIconEnd = Color(0xFFFFF9C4) // 浅黄
