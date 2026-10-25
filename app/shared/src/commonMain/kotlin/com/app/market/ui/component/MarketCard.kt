package com.app.market.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.market.ui.theme.MarketShapeTokens

/**
 * Material 3 卡片封装：`Card` + 大圆角与 `surfaceContainerLow` 容器色，默认零阴影（靠色调分层）。
 */
@Composable
fun MarketCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    cornerRadius: Dp = MarketShapeTokens.CardCorner,
    content: @Composable ColumnScope.() -> Unit,
) {
    val clickModifier = when {
        onClick != null && onLongClick != null -> Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        )

        onClick != null -> Modifier.combinedClickable(onClick = onClick)
        else -> Modifier
    }
    Card(
        modifier = modifier.then(clickModifier),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * 被拆成多个 lazy item 的「同一张卡片」中的一段：首段负责上圆角、末段负责下圆角，
 * 中间段只有底色，连续排列后视觉上是一张完整的 [MarketCard]。
 */
@Composable
fun CardSegmentContainer(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val topCorner = if (isFirst) MarketShapeTokens.CardCorner else 0.dp
    val bottomCorner = if (isLast) MarketShapeTokens.CardCorner else 0.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .clip(
                RoundedCornerShape(
                    topStart = topCorner,
                    topEnd = topCorner,
                    bottomStart = bottomCorner,
                    bottomEnd = bottomCorner,
                ),
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

/** 列表行内的分隔线：两端内缩，颜色用 outlineVariant。 */
@Composable
fun MarketDivider(
    modifier: Modifier = Modifier,
    startPadding: Dp = 16.dp,
    endPadding: Dp = 16.dp,
) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier.padding(start = startPadding, end = endPadding),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
}
