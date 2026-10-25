package com.app.market.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 组件级形状令牌：M3E 倾向更大的圆角与胶囊形，保证「大而圆」的视觉语言。 */
object MarketShapeTokens {
    /** 应用图标 / 小缩略图。 */
    val IconCorner = 16.dp

    /** 列表卡片、区块容器。 */
    val CardCorner = 28.dp

    /** 内嵌卡片（卡片里的卡片）。 */
    val InnerCardCorner = 20.dp

    /** 弹窗 / 底部面板。 */
    val SheetCorner = 28.dp

    /** 按钮。 */
    val ButtonCorner = 100.dp

    /** 标签、徽标。 */
    val TagCorner = 100.dp
}

internal val MarketShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
