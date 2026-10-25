package com.app.market.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.market.resources.Res
import com.app.market.resources.back
import com.app.market.ui.util.rememberIsWideScreen
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * 页面出入距：所有滚动页面「顶栏底部 → 内容顶部」「内容末尾 → 页面底部」的额外间距。
 */
val PageVerticalPadding = 12.dp

/** 页面 / 区块之间的分组间距（M3E 更大的呼吸感）。 */
val SectionSpacing = 20.dp

/** 卡片的水平外边距。 */
val PageHorizontalPadding = 16.dp

/**
 * 二级页脚手架：宽屏用固定小顶栏，窄屏用大标题可折叠顶栏。
 *
 * [content] 收到脚手架内边距、一个占位 [Modifier]（保留旧签名以兼容调用方，M3 用不透明顶栏，
 * 不再需要毛玻璃回调）以及用于 `nestedScroll` 的 [TopAppBarScrollBehavior]。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    content: @Composable (
        innerPadding: PaddingValues,
        backdropModifier: Modifier,
        scrollBehavior: TopAppBarScrollBehavior,
    ) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isWideScreen = rememberIsWideScreen()
    val colors = appBarColors()

    Scaffold(
        modifier = modifier,
        topBar = {
            val navigationIcon: @Composable () -> Unit = {
                BackButton(onClick = onBack)
            }
            if (isWideScreen) {
                TopAppBar(
                    title = { AppBarTitle(title, MaterialTheme.typography.titleLarge) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = colors,
                    scrollBehavior = scrollBehavior,
                )
            } else {
                LargeTopAppBar(
                    title = { AppBarTitle(title, MaterialTheme.typography.headlineMedium) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = colors,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = { bottomContent(scrollBehavior) },
        contentWindowInsets = WindowInsets.safeDrawing
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        content(innerPadding, Modifier, scrollBehavior)
    }
}

/**
 * 主页签脚手架：大标题顶栏，无返回按钮；底栏由外层 MainScreen 统一持有。
 *
 * [content] 收到顶栏占用的高度（供列表 `contentPadding` 使用）、占位 [Modifier] 和滚动行为。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScaffold(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (
        topPadding: Dp,
        backdropModifier: Modifier,
        scrollBehavior: TopAppBarScrollBehavior,
    ) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { AppBarTitle(title, MaterialTheme.typography.headlineMedium) },
                actions = actions,
                colors = appBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        content(innerPadding.calculateTopPadding(), Modifier, scrollBehavior)
    }
}

@Composable
private fun AppBarTitle(text: String, style: TextStyle) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = style,
    )
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.back),
        )
    }
}

/** 顶栏配色：M3E 用 surface 容器色，滚动时保持不透明以避免内容穿透。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

/**
 * 区块小标题：上方 20dp 由标题自身提供，标题到内容 8dp，水平 20dp。
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    topPadding: Dp = SectionSpacing,
) {
    Text(
        text = text,
        modifier = modifier.padding(start = 20.dp, top = topPadding, end = 20.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** 页面内容内边距辅助：左右 16dp、顶部额外 12dp。 */
fun pageContentPadding(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    horizontal: Dp = PageHorizontalPadding,
): PaddingValues = PaddingValues(
    start = horizontal,
    top = top + PageVerticalPadding,
    end = horizontal,
    bottom = bottom + PageVerticalPadding,
)

/** 供顶栏透明覆盖内容时使用的占位颜色。 */
internal val TransparentBarColor = Color.Transparent
