package com.app.market.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

/** 品牌种子色：系统不支持动态取色时（如 Android 11 及以下、桌面端）用它生成整套 Monet 配色。 */
private val BrandSeedColor = Color(0xFF4A3AFF)

/**
 * 全应用主题：与 KernelSU 的 Monet 取色保持一致。
 *
 * - Android 12+ 取系统动态色的 `primary` 作为种子；
 * - 不支持动态取色时回退品牌种子色；
 * - 统一用 Material Color Utilities（material-kolor）按 TonalSpot 生成完整色板，
 *   深浅色切换时逐色做弹簧过渡；
 * - 字体使用 Material 3 默认字阶（系统字体），动效使用 Expressive motion scheme。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val platformSeed = if (dynamicColor) rememberPlatformSeedColor(darkTheme) else null
    val seedColor = platformSeed ?: BrandSeedColor

    val colorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        isAmoled = false,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2021,
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme.animateAsState(),
        motionScheme = MotionScheme.expressive(),
        shapes = MarketShapes,
        typography = MarketTypography,
        content = content,
    )
}

/** 平台动态取色的种子色；不支持时返回 null，由品牌种子色兜底。 */
@Composable
expect fun rememberPlatformSeedColor(darkTheme: Boolean): Color?
