package com.app.market.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * MD3 Expressive 品牌色板：高彩度主色 + 高对比容器色。
 * 主色为电光靛蓝，辅色为青绿，第三色为暖橙，形成 M3E 强调「鲜明、成对、可对比」的配色关系。
 */
private val LightPrimary = Color(0xFF4A3AFF)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFE2DEFF)
private val LightOnPrimaryContainer = Color(0xFF12005E)
private val LightSecondary = Color(0xFF00696B)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFF9CF1F2)
private val LightOnSecondaryContainer = Color(0xFF002020)
private val LightTertiary = Color(0xFF9C3E00)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFFFDBC8)
private val LightOnTertiaryContainer = Color(0xFF341100)

internal val MarketLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = Color(0xFFC3BFFF),
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = Color(0xFFFDF8FF),
    onBackground = Color(0xFF1B1B22),
    surface = Color(0xFFFDF8FF),
    onSurface = Color(0xFF1B1B22),
    surfaceVariant = Color(0xFFE5E0F0),
    onSurfaceVariant = Color(0xFF47464F),
    surfaceTint = LightPrimary,
    inverseSurface = Color(0xFF303037),
    inverseOnSurface = Color(0xFFF3EFF8),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF78767F),
    outlineVariant = Color(0xFFC9C5D1),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFDF8FF),
    surfaceDim = Color(0xFFDDD8E3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FC),
    surfaceContainer = Color(0xFFF1ECF6),
    surfaceContainerHigh = Color(0xFFEBE6F0),
    surfaceContainerHighest = Color(0xFFE5E0EA),
)

internal val MarketDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC3BFFF),
    onPrimary = Color(0xFF1F00A4),
    primaryContainer = Color(0xFF3223C9),
    onPrimaryContainer = Color(0xFFE2DEFF),
    inversePrimary = LightPrimary,
    secondary = Color(0xFF80D4D5),
    onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF004F50),
    onSecondaryContainer = Color(0xFF9CF1F2),
    tertiary = Color(0xFFFFB693),
    onTertiary = Color(0xFF552000),
    tertiaryContainer = Color(0xFF7A3000),
    onTertiaryContainer = Color(0xFFFFDBC8),
    background = Color(0xFF131319),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131319),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC9C5D1),
    surfaceTint = Color(0xFFC3BFFF),
    inverseSurface = Color(0xFFE5E1E9),
    inverseOnSurface = Color(0xFF303037),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF928F9A),
    outlineVariant = Color(0xFF47464F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF393840),
    surfaceDim = Color(0xFF131319),
    surfaceContainerLowest = Color(0xFF0D0D13),
    surfaceContainerLow = Color(0xFF1B1B21),
    surfaceContainer = Color(0xFF1F1F25),
    surfaceContainerHigh = Color(0xFF2A2930),
    surfaceContainerHighest = Color(0xFF35343B),
)
