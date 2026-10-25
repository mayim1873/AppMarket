package com.app.market.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

/**
 * 全应用主题入口：Material 3 Expressive。
 *
 * - 颜色：Android 12+ 优先取系统动态取色（Material You），否则使用品牌高彩度色板；
 * - 动效：Expressive motion scheme（弹簧驱动的空间动效）；
 * - 字体：大标题加粗 + 收紧字距的 M3E 层级；
 * - 形状：大圆角（卡片 28dp、按钮胶囊）。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dynamicScheme = if (dynamicColor) rememberPlatformColorScheme(darkTheme) else null
    val colorScheme = dynamicScheme ?: if (darkTheme) MarketDarkColorScheme else MarketLightColorScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = MarketShapes,
        typography = MarketTypography,
        content = content,
    )
}

/** 平台动态取色；不支持时返回 null，由品牌色板兜底。 */
@Composable
expect fun rememberPlatformColorScheme(darkTheme: Boolean): ColorScheme?
