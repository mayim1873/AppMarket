package com.app.market.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.today.TodayArticle
import com.app.market.domain.model.today.TodayFeaturedItem
import com.app.market.resources.Res
import com.app.market.resources.golden_award
import com.app.market.resources.nav_today
import com.app.market.resources.num_updates_pending
import com.app.market.resources.view
import com.app.market.ui.component.AppAsyncImage
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.theme.MarketShapeTokens
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.todayAwardLabel
import com.app.market.ui.util.todayAppSummary
import com.app.market.viewmodel.TodayViewModel
import com.app.market.viewmodel.UpdatesViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil

private val TodayCardRadius = MarketShapeTokens.CardCorner
private val TodayGridMinCellWidth = 320.dp

/**
 * 今日页：M3E 沉浸式信息流。页面自绘大标题（随内容滚动），瀑布卡片使用 28dp 圆角，
 * 封面图叠加同色系渐变蒙版承载编辑文案。
 */
@Composable
fun TodayTab(
    viewModel: TodayViewModel,
    onClickArticle: (TodayArticle) -> Unit,
    updatesViewModel: UpdatesViewModel? = null,
    onClickViewUpdates: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState = updatesViewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val listState = rememberLazyGridState()
    val currentState by rememberUpdatedState(state)
    val pendingUpdates =
        if (updateState != null && !updateState.loading) updateState.updates else emptyList()
    val showUpdatesCard = pendingUpdates.isNotEmpty()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(showUpdatesCard) {
        if (listState.firstVisibleItemIndex <= 1 && listState.firstVisibleItemScrollOffset == 0) {
            listState.requestScrollToItem(0)
        }
    }

    LaunchedEffect(listState, viewModel) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val nearEnd = lastVisible >= 0 &&
                    layoutInfo.totalItemsCount > 0 &&
                    lastVisible >= layoutInfo.totalItemsCount - 3
            nearEnd &&
                    !currentState.feedLoading &&
                    !currentState.feedLoadingMore &&
                    currentState.feed.hasMore &&
                    currentState.feedError.isBlank()
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore -> if (shouldLoadMore) viewModel.loadMore() }
    }

    val initialLoading = state.feedLoading && state.feed.items.isEmpty()
    Crossfade(
        targetState = initialLoading,
        modifier = Modifier.fillMaxSize(),
        label = "today",
    ) { loading ->
        if (loading) {
            LoadingBox(
                Modifier.fillMaxSize().padding(
                    top = statusBarTop + PageVerticalPadding,
                    bottom = PageVerticalPadding,
                ),
            )
            return@Crossfade
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(TodayGridMinCellWidth),
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = statusBarTop + 8.dp,
                bottom = PageVerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "title", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(Res.string.nav_today),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            if (showUpdatesCard) {
                item(
                    key = "updates",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    var pendingCardVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { pendingCardVisible = true }
                    AnimatedVisibility(
                        visible = pendingCardVisible,
                        enter = fadeIn(tween(220)) +
                                expandVertically(tween(300), expandFrom = Alignment.Top),
                    ) {
                        PendingUpdatesCard(
                            updates = pendingUpdates,
                            onClick = onClickViewUpdates,
                        )
                    }
                }
            }
            items(state.feed.items, key = { "feed-${it.rId.ifBlank { it.articleLink }}" }) { item ->
                FeaturedArticleCard(
                    item = item,
                    fullCoverOverlay = state.source.capabilities.todayFullCoverOverlay,
                    modifier = Modifier.animateItem(),
                ) {
                    openArticle(item, onClickArticle)
                }
            }
            if (state.feedLoading || state.feedLoadingMore) {
                item(
                    key = "feed-loading",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    LoadingBox(Modifier.fillMaxWidth())
                }
            }
            if (state.feedError.isNotBlank()) {
                item(
                    key = "feed-error",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Text(
                        text = state.feedError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = viewModel::retryFeed)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun openArticle(item: TodayFeaturedItem, onClickArticle: (TodayArticle) -> Unit) {
    val articleId = item.rId.ifBlank { return }
    onClickArticle(
        TodayArticle(
            rId = articleId,
            title = item.title,
            awardName = item.awardName,
            headerImage = item.coverImage,
            richTextHtml = item.summary,
            app = item.app ?: item.apps.singleOrNull(),
            apps = item.apps,
            showTitleLabel = item.showTitleLabel,
            showCoverText = item.showCoverText,
        ),
    )
}

/** 待更新提示卡：primaryContainer 色块 + 图标流 + 「查看」按钮。 */
@Composable
private fun PendingUpdatesCard(
    updates: List<MarketAppInfo>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstRow = remember(updates) { updates.filterIndexed { index, _ -> index % 2 == 0 } }
    val secondRow = remember(updates) { updates.filterIndexed { index, _ -> index % 2 == 1 } }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val rowCapacity = ((maxWidth - 8.dp) / 80.dp).toInt().coerceAtLeast(1)
        val fitsOneRow = updates.size <= rowCapacity
        val singleRowLayout = updates.size <= rowCapacity * 2

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TodayCardRadius))
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 18.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.num_updates_pending, updates.size),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    AppButton(
                        text = stringResource(Res.string.view),
                        onClick = onClick,
                        minHeight = 38.dp,
                        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    when {
                        fitsOneRow -> StaticIconRow(updates)
                        singleRowLayout -> ScrollingIconRow(updates, moveLeft = true)
                        else -> Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            ScrollingIconRow(firstRow, moveLeft = true)
                            ScrollingIconRow(secondRow, moveLeft = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticIconRow(
    apps: List<MarketAppInfo>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    itemWidth: Dp = 80.dp,
    rowHeight: Dp = 64.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(start = 20.dp - (itemWidth - iconSize) / 2),
        horizontalArrangement = Arrangement.Start,
    ) {
        apps.forEach { app ->
            Box(
                modifier = Modifier.size(width = itemWidth, height = rowHeight),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    url = app.icon,
                    contentDescription = appDisplayName(app.displayName, app.source),
                    size = iconSize,
                )
            }
        }
    }
}

@Composable
internal fun ScrollingIconRow(
    apps: List<MarketAppInfo>,
    moveLeft: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    itemWidth: Dp = 80.dp,
    rowHeight: Dp = 64.dp,
) {
    if (apps.isEmpty()) {
        Box(modifier.fillMaxWidth().height(rowHeight))
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
    ) {
        val minLoopItems = ceil(maxWidth / itemWidth).toInt().coerceAtLeast(1)
        val loopApps = remember(apps, minLoopItems) {
            if (apps.size >= minLoopItems) {
                apps
            } else {
                List(minLoopItems) { index -> apps[index % apps.size] }
            }
        }

        val transition = rememberInfiniteTransition(label = if (moveLeft) "left" else "right")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (loopApps.size * 2500).coerceAtLeast(12_000),
                    easing = LinearEasing,
                ),
            ),
            label = "progress",
        )

        val cycleWidth = itemWidth * loopApps.size
        val cycleWidthPx = with(LocalDensity.current) { cycleWidth.toPx() }

        repeat(2) { copyIndex ->
            Row(
                modifier = Modifier
                    .wrapContentWidth(unbounded = true, align = Alignment.Start)
                    .width(cycleWidth)
                    .graphicsLayer {
                        translationX = if (moveLeft) {
                            copyIndex * cycleWidthPx - progress * cycleWidthPx
                        } else {
                            (copyIndex - 1) * cycleWidthPx + progress * cycleWidthPx
                        }
                    },
            ) {
                loopApps.forEachIndexed { index, app ->
                    Box(
                        modifier = Modifier.size(width = itemWidth, height = rowHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(
                            url = app.icon,
                            contentDescription = if (copyIndex == 0 && index < apps.size) {
                                appDisplayName(app.displayName, app.source)
                            } else {
                                null
                            },
                            size = iconSize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdaptiveAppIconRow(
    apps: List<MarketAppInfo>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    itemSpacing: Dp = 16.dp,
) {
    BoxWithConstraints(modifier = modifier.height(iconSize)) {
        val requiredWidth = if (apps.isEmpty()) {
            0.dp
        } else {
            iconSize * apps.size + itemSpacing * (apps.size - 1)
        }
        if (requiredWidth > maxWidth) {
            ScrollingIconRow(
                apps = apps,
                moveLeft = true,
                iconSize = iconSize,
                itemWidth = iconSize + itemSpacing,
                rowHeight = iconSize,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(iconSize),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                apps.forEach { app ->
                    AppIcon(
                        url = app.icon,
                        contentDescription = appDisplayName(app.displayName, app.source),
                        size = iconSize,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedArticleCard(
    item: TodayFeaturedItem,
    modifier: Modifier = Modifier,
    fullCoverOverlay: Boolean = false,
    onClick: () -> Unit,
) {
    val fallbackColor = MaterialTheme.colorScheme.primary
    var gradientColor by remember(item.coverImage, fallbackColor) { mutableStateOf(fallbackColor) }
    var coverRatio by remember(item.coverImage) { mutableStateOf<Float?>(null) }
    val app = item.app ?: item.apps.singleOrNull()
    val hasCover = item.coverImage.isNotBlank()
    val coverSizeModifier = Modifier
        .fillMaxWidth()
        .then(
            if (coverRatio != null) {
                Modifier.aspectRatio(coverRatio!!)
            } else {
                Modifier.height(330.dp)
            }
        )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TodayCardRadius))
            .background(gradientColor)
            .clickable(onClick = onClick),
    ) {
        // 小米今日整卡覆盖：两行文案与应用行都叠加在封面上；其余来源保持两行文案覆盖 + 应用行附加。
        if (fullCoverOverlay && hasCover) {
            Box(coverSizeModifier) {
                FeaturedCoverImage(
                    item = item,
                    modifier = Modifier.fillMaxSize(),
                    onGradientColor = { gradientColor = it },
                    onCoverSize = { width, height -> coverRatio = width.toFloat() / height },
                )
                if (item.showCoverText) {
                    CoverScrim(gradientColor)
                    Column(Modifier.fillMaxWidth().align(Alignment.BottomStart)) {
                        EditorialFeatureContent(item)
                        if (app != null) AppFeatureContent(app, appBarColor = Color.Transparent)
                    }
                } else if (app != null) {
                    AppFeatureContent(
                        app = app,
                        appBarColor = Color.Transparent,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        } else {
            Column(Modifier.fillMaxWidth()) {
                if (hasCover) {
                    Box(coverSizeModifier) {
                        FeaturedCoverImage(
                            item = item,
                            modifier = Modifier.fillMaxSize(),
                            onGradientColor = { gradientColor = it },
                            onCoverSize = { width, height -> coverRatio = width.toFloat() / height },
                        )
                        if (item.showCoverText) {
                            CoverScrim(gradientColor)
                            EditorialFeatureContent(item, Modifier.align(Alignment.BottomStart))
                        }
                    }
                }
                if (app != null) {
                    AppFeatureContent(
                        app = app,
                        appBarColor = if (hasCover && item.showCoverText) null else gradientColor,
                    )
                } else if (item.showCoverText) {
                    EditorialFeatureContent(item)
                }
            }
        }
    }
}

@Composable
private fun FeaturedCoverImage(
    item: TodayFeaturedItem,
    modifier: Modifier = Modifier,
    onGradientColor: (Color) -> Unit,
    onCoverSize: (width: Int, height: Int) -> Unit,
) {
    AppAsyncImage(
        url = item.coverImage,
        contentDescription = item.title,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        onDominantColor = onGradientColor,
        onLoaded = { image ->
            if (image.width > 0 && image.height > 0) {
                onCoverSize(image.width, image.height)
            }
        },
        showLoadingIndicator = true,
        loadingIndicatorAlignment = BiasAlignment(0f, -0.25f),
        loadingIndicatorColor = Color.White,
    )
}

@Composable
private fun CoverScrim(color: Color) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to color.copy(alpha = 0f),
                    0.54f to color.copy(alpha = 0f),
                    0.76f to color,
                    1f to color,
                ),
            ),
    )
}

@Composable
private fun EditorialFeatureContent(
    item: TodayFeaturedItem,
    modifier: Modifier = Modifier,
) {
    val label = item.awardName.ifBlank { stringResource(Res.string.golden_award) }
    val appName = (item.app ?: item.apps.singleOrNull())?.displayName
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
    ) {
        if (item.showTitleLabel) {
            FeatureLabel(todayAwardLabel(label, item.title, appName))
        }
        FeatureTitle(item.summary.ifBlank { item.title })
    }
}

@Composable
private fun AppFeatureContent(
    app: MarketAppInfo,
    appBarColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(appBarColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            url = app.icon,
            contentDescription = appDisplayName(app.displayName, app.source),
            size = 52.dp,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = appDisplayName(app.displayName, app.source),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FeatureSummary(app.todayAppSummary())
        }
    }
}

@Composable
private fun FeatureLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.75f),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun FeatureTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun FeatureSummary(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.7f),
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
