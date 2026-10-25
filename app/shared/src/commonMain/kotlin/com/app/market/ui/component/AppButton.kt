package com.app.market.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.market.domain.model.download.DownloadPhase
import com.app.market.domain.model.download.DownloadState
import com.app.market.resources.Res
import com.app.market.resources.download_paused
import com.app.market.resources.downloading_short
import com.app.market.resources.install
import com.app.market.resources.installing_short
import com.app.market.resources.waiting_install_confirmation
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.theme.MarketShapeTokens
import org.jetbrains.compose.resources.stringResource

/** 按钮标签最小字号：超宽时自动缩字至 8sp 再省略。 */
private val AppButtonMinFontSize = 8.sp

/** 应用内统一的按钮配色：容器色 / 内容色 + 禁用态。 */
@Immutable
data class AppButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

object AppButtonDefaults {
    val MinWidth: Dp = 64.dp
    val MinHeight: Dp = 40.dp
    val InsideMargin: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 10.dp)

    /** 主按钮：高对比填充（primary）。 */
    @Composable
    fun filled(): AppButtonColors = AppButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    /** 次按钮：中性容器（surfaceContainerHighest）。 */
    @Composable
    fun neutral(): AppButtonColors = AppButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    /** 强调次按钮：主色浅容器（primaryContainer 的轻量版）。 */
    @Composable
    fun tonal(): AppButtonColors {
        val container = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                .compositeOver(MaterialTheme.colorScheme.surface)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        return AppButtonColors(
            containerColor = container,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    }

    /** 文本按钮：透明容器。 */
    @Composable
    fun text(): AppButtonColors = AppButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = MarketShapeTokens.ButtonCorner,
    minWidth: Dp = AppButtonDefaults.MinWidth,
    minHeight: Dp = AppButtonDefaults.MinHeight,
    colors: AppButtonColors = AppButtonDefaults.filled(),
    insideMargin: PaddingValues = AppButtonDefaults.InsideMargin,
) {
    AppButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        cornerRadius = cornerRadius,
        minWidth = minWidth,
        minHeight = minHeight,
        colors = colors,
        insideMargin = insideMargin,
    ) {
        AppButtonText(text)
    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = MarketShapeTokens.ButtonCorner,
    minWidth: Dp = AppButtonDefaults.MinWidth,
    minHeight: Dp = AppButtonDefaults.MinHeight,
    colors: AppButtonColors = AppButtonDefaults.filled(),
    insideMargin: PaddingValues = AppButtonDefaults.InsideMargin,
    content: @Composable RowScope.() -> Unit,
) {
    val container = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    Row(
        modifier = modifier
            .widthIn(min = minWidth)
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(insideMargin),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = MarketShapeTokens.ButtonCorner,
    minWidth: Dp = 0.dp,
    minHeight: Dp = AppButtonDefaults.MinHeight,
    colors: AppButtonColors = AppButtonDefaults.text(),
    insideMargin: PaddingValues = AppButtonDefaults.InsideMargin,
) {
    AppButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        cornerRadius = cornerRadius,
        minWidth = minWidth,
        minHeight = minHeight,
        colors = colors,
        insideMargin = insideMargin,
    ) {
        AppButtonText(text)
    }
}

/** 独立的按钮文本，颜色默认继承按钮内容色。 */
@Composable
fun AppButtonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val style = MaterialTheme.typography.labelLarge
    val maxFontSize = style.fontSize.takeUnless { it == TextUnit.Unspecified } ?: 14.sp
    val minFontSize = if (maxFontSize.value < AppButtonMinFontSize.value) maxFontSize else AppButtonMinFontSize
    Text(
        text = text,
        modifier = modifier,
        color = color ?: Color.Unspecified,
        autoSize = DescendingSpAutoSize(minFontSize = minFontSize, maxFontSize = maxFontSize),
        style = style,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
    )
}

private data class DescendingSpAutoSize(
    val minFontSize: TextUnit,
    val maxFontSize: TextUnit,
) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(
        constraints: Constraints,
        text: AnnotatedString,
    ): TextUnit {
        var currentSp = maxFontSize.value
        while (currentSp > minFontSize.value) {
            val result = performLayout(constraints, text, currentSp.sp)
            val overflowed = result.hasVisualOverflow ||
                    (result.lineCount > 0 && result.isLineEllipsized(0))
            if (!overflowed) return currentSp.sp
            currentSp -= 0.5f
        }
        return minFontSize
    }
}

private val ActionButtonMinHeight = 34.dp
private val ActionButtonInsideMargin = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
private val ActiveDownloadPhases = setOf(
    DownloadPhase.QUEUED,
    DownloadPhase.DOWNLOADING,
    DownloadPhase.PAUSED,
    DownloadPhase.INSTALLING,
    DownloadPhase.AWAITING_USER_ACTION,
)
private val CancelableDownloadPhases = setOf(
    DownloadPhase.QUEUED,
    DownloadPhase.DOWNLOADING,
    DownloadPhase.AWAITING_USER_ACTION,
)

@Composable
fun AppCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: AppButtonColors = AppButtonDefaults.tonal(),
    minHeight: Dp = ActionButtonMinHeight,
    insideMargin: PaddingValues = ActionButtonInsideMargin,
) {
    AppButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        cornerRadius = MarketShapeTokens.ButtonCorner,
        minHeight = minHeight,
        insideMargin = insideMargin,
        modifier = modifier,
    )
}

@Composable
fun AppActionButton(
    packageName: String,
    actionText: String,
    actionKind: AppActionKind,
    downloadState: DownloadState?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    minHeight: Dp = ActionButtonMinHeight,
    insideMargin: PaddingValues = ActionButtonInsideMargin,
    onAction: () -> Unit,
    onResumeDownload: () -> Unit,
    onInstallDownloaded: (String) -> Unit,
    onCancel: (String) -> Unit,
) {
    // 无账号体系可执行预约，按钮仅作状态展示。
    if (actionKind == AppActionKind.RESERVE) {
        AppCompactButton(
            text = actionText,
            onClick = {},
            enabled = false,
            modifier = modifier,
            minHeight = minHeight,
            insideMargin = insideMargin,
        )
        return
    }

    val activePhase = downloadState?.phase in ActiveDownloadPhases
    // 下载 / 安装进行中：按钮本身成为进度指示器。
    if (downloadState != null && activePhase) {
        val percent = downloadState.progress
        DownloadProgressButton(
            label = when (downloadState.phase) {
                DownloadPhase.PAUSED -> stringResource(Res.string.download_paused)
                DownloadPhase.INSTALLING -> stringResource(Res.string.installing_short)
                DownloadPhase.AWAITING_USER_ACTION -> stringResource(Res.string.waiting_install_confirmation)
                else -> percent?.let { "$it%" } ?: stringResource(Res.string.downloading_short)
            },
            fraction = (percent ?: 0).coerceIn(0, 100) / 100f,
            onClick = {
                if (downloadState.isPaused) {
                    onResumeDownload()
                } else if (downloadState.phase in CancelableDownloadPhases) {
                    onCancel(packageName)
                }
            },
            modifier = modifier,
            minHeight = minHeight,
            insideMargin = insideMargin,
        )
        return
    }

    val buttonText = when {
        // 已下载完成，等待用户安装。
        downloadState?.isComplete == true -> stringResource(Res.string.install)
        else -> actionText
    }
    val click: () -> Unit =
        if (downloadState?.isComplete == true) ({ onInstallDownloaded(packageName) }) else onAction
    // 「打开」（已安装）保持中性外观；可执行动作使用填充高亮样式。
    val neutral = downloadState == null && actionKind == AppActionKind.OPEN
    AppCompactButton(
        text = buttonText,
        onClick = click,
        enabled = enabled,
        modifier = modifier,
        colors = if (neutral) AppButtonDefaults.neutral() else AppButtonDefaults.filled(),
        minHeight = minHeight,
        insideMargin = insideMargin,
    )
}

/**
 * 下载进度按钮：主色填充从左向右生长，文字在填充边界处切换对比色。
 */
@Composable
private fun DownloadProgressButton(
    label: String,
    fraction: Float,
    modifier: Modifier = Modifier,
    minHeight: Dp = ActionButtonMinHeight,
    insideMargin: PaddingValues = ActionButtonInsideMargin,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val baseTextColor = if (base.luminance() > 0.5f) Color(0xFF1B1B22) else Color.White
    val animatedFraction = animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "downloadFill",
    )
    Box(
        modifier = modifier
            .widthIn(min = AppButtonDefaults.MinWidth)
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(MarketShapeTokens.ButtonCorner))
            .drawBehind { drawRect(base) }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 主色填充（在绘制阶段读取动画值）。
        Box(
            Modifier
                .matchParentSize()
                .drawBehind {
                    val filledWidth = size.width * animatedFraction.value
                    if (filledWidth > 0f) {
                        drawRect(color = primary, size = Size(filledWidth, size.height))
                    }
                },
        )
        // 底色文字：在未填充区域可见，同时决定按钮尺寸。
        AppButtonText(
            text = label,
            color = baseTextColor,
            modifier = Modifier.padding(insideMargin),
        )
        // 填充区文字：裁剪到填充范围内，覆盖底色文字。
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    clipRect(right = size.width * animatedFraction.value) {
                        this@drawWithContent.drawContent()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            AppButtonText(
                text = label,
                color = onPrimary,
                modifier = Modifier.padding(insideMargin),
            )
        }
    }
}
