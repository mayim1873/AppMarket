package com.app.market.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 颜色过渡：主题切换（深浅色 / 取色变化）时逐色动画，与 KernelSU 的实现一致。
 */
@Composable
fun ColorScheme.animateAsState(): ColorScheme {
    @Composable
    fun animate(color: Color): Color = animateColorAsState(
        targetValue = color,
        animationSpec = spring(),
        label = "theme_color_animation",
    ).value

    return ColorScheme(
        primary = animate(primary),
        onPrimary = animate(onPrimary),
        primaryContainer = animate(primaryContainer),
        onPrimaryContainer = animate(onPrimaryContainer),
        inversePrimary = animate(inversePrimary),
        secondary = animate(secondary),
        onSecondary = animate(onSecondary),
        secondaryContainer = animate(secondaryContainer),
        onSecondaryContainer = animate(onSecondaryContainer),
        tertiary = animate(tertiary),
        onTertiary = animate(onTertiary),
        tertiaryContainer = animate(tertiaryContainer),
        onTertiaryContainer = animate(onTertiaryContainer),
        background = animate(background),
        onBackground = animate(onBackground),
        surface = animate(surface),
        onSurface = animate(onSurface),
        surfaceVariant = animate(surfaceVariant),
        onSurfaceVariant = animate(onSurfaceVariant),
        surfaceTint = animate(surfaceTint),
        inverseSurface = animate(inverseSurface),
        inverseOnSurface = animate(inverseOnSurface),
        error = animate(error),
        onError = animate(onError),
        errorContainer = animate(errorContainer),
        onErrorContainer = animate(onErrorContainer),
        outline = animate(outline),
        outlineVariant = animate(outlineVariant),
        scrim = animate(scrim),
        surfaceBright = animate(surfaceBright),
        surfaceDim = animate(surfaceDim),
        surfaceContainer = animate(surfaceContainer),
        surfaceContainerHigh = animate(surfaceContainerHigh),
        surfaceContainerHighest = animate(surfaceContainerHighest),
        surfaceContainerLow = animate(surfaceContainerLow),
        surfaceContainerLowest = animate(surfaceContainerLowest),
        primaryFixed = animate(primaryFixed),
        primaryFixedDim = animate(primaryFixedDim),
        onPrimaryFixed = animate(onPrimaryFixed),
        onPrimaryFixedVariant = animate(onPrimaryFixedVariant),
        secondaryFixed = animate(secondaryFixed),
        secondaryFixedDim = animate(secondaryFixedDim),
        onSecondaryFixed = animate(onSecondaryFixed),
        onSecondaryFixedVariant = animate(onSecondaryFixedVariant),
        tertiaryFixed = animate(tertiaryFixed),
        tertiaryFixedDim = animate(tertiaryFixedDim),
        onTertiaryFixed = animate(onTertiaryFixed),
        onTertiaryFixedVariant = animate(onTertiaryFixedVariant),
    )
}
