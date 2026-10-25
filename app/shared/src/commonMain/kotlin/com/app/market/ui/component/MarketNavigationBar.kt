package com.app.market.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 底部 / 侧边导航的一个目的地。
 *
 * 与 Material 规范一致：选中态用实心图标，未选中态用轮廓图标。
 */
data class MarketNavItem(
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0,
    val unselectedIcon: ImageVector = icon,
)

/**
 * 标准 Material 3 底栏：`ShortNavigationBar`（M3E）作为 Scaffold 的 bottomBar，
 * 通栏、内建系统栏内边距，选中项由组件自身绘制药丸指示器。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarketNavigationBar(
    items: List<MarketNavItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShortNavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        windowInsets = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            ShortNavigationBarItem(
                selected = selected,
                onClick = { onSelected(index) },
                icon = {
                    NavBarIcon(
                        icon = if (selected) item.icon else item.unselectedIcon,
                        contentDescription = item.label,
                        badgeCount = item.badgeCount,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun NavBarIcon(
    icon: ImageVector,
    contentDescription: String?,
    badgeCount: Int,
) {
    if (badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(badgeCount.toString())
                }
            },
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    } else {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/**
 * 标准 Material 3 Expressive 侧边导航：`WideNavigationRail`，可在收起 / 展开之间切换。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarketNavigationRail(
    items: List<MarketNavItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberWideNavigationRailState(
        initialValue = if (expanded) {
            WideNavigationRailValue.Expanded
        } else {
            WideNavigationRailValue.Collapsed
        },
    )
    val scope = rememberCoroutineScope()
    val isExpanded = state.targetValue == WideNavigationRailValue.Expanded
    LaunchedEffect(state.targetValue) {
        onExpandedChange(state.targetValue == WideNavigationRailValue.Expanded)
    }

    WideNavigationRail(
        modifier = modifier.fillMaxHeight(),
        state = state,
        colors = WideNavigationRailDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        windowInsets = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
        header = {
            IconButton(
                modifier = Modifier.padding(start = 24.dp),
                onClick = {
                    scope.launch {
                        if (isExpanded) state.collapse() else state.expand()
                    }
                },
            ) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.AutoMirrored.Filled.MenuOpen
                    } else {
                        Icons.Filled.Menu
                    },
                    contentDescription = null,
                )
            }
        },
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            WideNavigationRailItem(
                railExpanded = isExpanded,
                selected = selected,
                onClick = { onSelected(index) },
                icon = {
                    NavBarIcon(
                        icon = if (selected) item.icon else item.unselectedIcon,
                        contentDescription = item.label,
                        badgeCount = item.badgeCount,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
