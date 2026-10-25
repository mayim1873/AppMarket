package com.app.market.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.today.TodayArticle
import com.app.market.domain.model.today.TodayArticleBlock
import com.app.market.resources.Res
import com.app.market.resources.back
import com.app.market.resources.golden_award
import com.app.market.ui.component.AppAsyncImage
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppButtonColors
import com.app.market.ui.component.AppButtonDefaults
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.RelatedAppsList
import com.app.market.ui.util.allowColorSampling
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.dominantImageColor
import com.app.market.ui.util.installActionText
import com.app.market.ui.util.todayAwardLabel
import com.app.market.ui.util.todayAppSummary
import com.app.market.viewmodel.TodayViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

// 头图预载最长等待，超时照常揭示
private const val ARTICLE_HEADER_IMAGE_WAIT_MS = 8_000L

/** 头图高度：编辑类 / 单应用文章。 */
private val ArticleHeroHeight = 447.dp

/** 头图高度：多应用合辑（底部还要放应用图标行）。 */
private val GroupHeroHeight = 509.dp

/** 顶栏标题开始淡入的滚动距离。 */
private val TopBarCollapseDistance = 160.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayArticleScreen(
    rId: String,
    viewModel: TodayViewModel,
    onOpenApp: (MarketAppInfo) -> Unit,
    onBack: () -> Unit,
    applyWindowInsets: Boolean = true,
) {
    LaunchedEffect(rId) { viewModel.loadArticle(rId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val article = state.article?.takeIf { it.rId == rId }
    val feedItem = state.feed.items.firstOrNull { it.rId == rId }
    val lazyListState = rememberLazyListState()
    val title = feedItem?.title?.ifBlank { article?.title.orEmpty() }
        ?: article?.title.orEmpty()
    val summary = feedItem?.summary.orEmpty()
    val awardName = feedItem?.awardName.orEmpty()
        .ifBlank { article?.awardName.orEmpty() }
        .ifBlank { stringResource(Res.string.golden_award) }
    val appName = feedItem?.app?.displayName
        ?: feedItem?.apps?.singleOrNull()?.displayName
        ?: article?.app?.displayName
        ?: article?.apps?.singleOrNull()?.displayName
    val awardLabel = todayAwardLabel(awardName, title, appName)
    val collapsedTitle = awardLabel
    val defaultAccentColor = MaterialTheme.colorScheme.primary
    var accentColor by remember(article?.headerImage, defaultAccentColor) {
        mutableStateOf(defaultAccentColor)
    }
    val isDeveloperLecture = rId.startsWith("developer-lecture:") ||
            (article?.showCoverText ?: feedItem?.showCoverText) == false

    // 头图预载完成前整页保持加载态；正文一旦揭示不再回退
    val platformContext = LocalPlatformContext.current
    var headerImageReady by remember(article?.headerImage) {
        mutableStateOf(article?.headerImage.isNullOrBlank())
    }
    LaunchedEffect(article?.headerImage) {
        val url = article?.headerImage?.takeIf(String::isNotBlank) ?: return@LaunchedEffect
        val preview = article.headerImagePreview.takeIf { it.isNotBlank() && it != url }
        val imageLoader = SingletonImageLoader.get(platformContext)
        withTimeoutOrNull(ARTICLE_HEADER_IMAGE_WAIT_MS.milliseconds) {
            coroutineScope {
                val requests = listOfNotNull(preview, url).map { candidate ->
                    async {
                        imageLoader.execute(
                            ImageRequest.Builder(platformContext)
                                .data(candidate)
                                .allowColorSampling()
                                .build()
                        )
                    }
                }
                val first = select { requests.forEach { request -> request.onAwait { it } } }
                requests.forEach { it.cancel() }
                first.image?.let { image -> dominantImageColor(image)?.let { accentColor = it } }
            }
        }
        headerImageReady = true
    }
    var contentRevealed by remember(rId) { mutableStateOf(false) }
    LaunchedEffect(article, headerImageReady) {
        val current = article ?: return@LaunchedEffect
        if (!contentRevealed && (current.apps.isEmpty() || headerImageReady)) {
            contentRevealed = true
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val collapseDistancePx = with(LocalDensity.current) { TopBarCollapseDistance.toPx() }
    // 逐帧变化的滚动进度只以 State 传递，读取下沉到 graphicsLayer / drawBehind，避免整页重组
    val collapseProgress = remember(lazyListState, collapseDistancePx) {
        derivedStateOf {
            when {
                lazyListState.firstVisibleItemIndex > 0 -> 1f
                else -> (lazyListState.firstVisibleItemScrollOffset / collapseDistancePx)
                    .coerceIn(0f, 1f)
            }
        }
    }
    val bottomInset = if (applyWindowInsets) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ArticleTopBar(
                title = collapsedTitle,
                onBack = onBack,
                showNavigationIcon = !isDeveloperLecture,
                // 开发者讲座没有封面文案，标题不能等滚动后才出现
                titleAlwaysVisible = isDeveloperLecture,
                scrollBehavior = scrollBehavior,
                collapseProgress = collapseProgress,
            )
        },
        contentWindowInsets = if (applyWindowInsets) {
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
        } else {
            WindowInsets(0, 0, 0, 0)
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        when {
            state.articleLoading ||
                    (article == null && state.articleError.isBlank()) ||
                    (article != null && !contentRevealed) ->
                ArticleLoadingState(safePadding = innerPadding)

            article == null -> ArticleErrorState(
                message = state.articleError,
                safePadding = innerPadding,
            )

            else -> {
                val currentArticle = article
                val bodyBlocks = remember(currentArticle.blocks, currentArticle.apps) {
                    orderedArticleBlocks(currentArticle).withoutBoundaryImages()
                }
                ArticleContent(
                    bodyBlocks = bodyBlocks,
                    app = currentArticle.apps.singleOrNull(),
                    hero = {
                        when (currentArticle.apps.size) {
                            0 -> if (currentArticle.headerImage.isNotBlank()) {
                                ArticleHero(
                                    imageUrl = currentArticle.headerImage,
                                    placeholderImageUrl = currentArticle.headerImagePreview,
                                    awardLabel = awardLabel,
                                    title = title,
                                    reason = summary,
                                    showTitleLabel = currentArticle.showTitleLabel,
                                    showCoverText = currentArticle.showCoverText,
                                    accentColor = accentColor,
                                    onAccentColor = { accentColor = it },
                                    height = ArticleHeroHeight,
                                )
                            }

                            1 -> ArticleHero(
                                imageUrl = currentArticle.headerImage,
                                placeholderImageUrl = currentArticle.headerImagePreview,
                                awardLabel = awardLabel,
                                title = title,
                                reason = summary,
                                showTitleLabel = currentArticle.showTitleLabel,
                                showCoverText = currentArticle.showCoverText,
                                accentColor = accentColor,
                                onAccentColor = { accentColor = it },
                                height = ArticleHeroHeight,
                            )

                            else -> GroupArticleHero(
                                imageUrl = currentArticle.headerImage,
                                placeholderImageUrl = currentArticle.headerImagePreview,
                                title = title,
                                summary = summary,
                                apps = currentArticle.apps,
                                accentColor = accentColor,
                                onAccentColor = { accentColor = it },
                            )
                        }
                    },
                    innerPadding = innerPadding,
                    bottomInset = bottomInset,
                    lazyListState = lazyListState,
                    scrollBehavior = scrollBehavior,
                    onOpenApp = onOpenApp,
                )
            }
        }
    }
}

/**
 * 沉浸式顶栏：无折叠位移的固定顶栏，容器在英雄图上保持透明，
 * 内容滚过首屏后由 [ArticleTopBar] 自己绘制 surface 遮罩并淡入标题。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleTopBar(
    title: String,
    onBack: () -> Unit,
    showNavigationIcon: Boolean,
    titleAlwaysVisible: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    collapseProgress: State<Float>,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onSurface
    val scrimColor = MaterialTheme.colorScheme.scrim
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                modifier = Modifier.graphicsLayer {
                    alpha = if (titleAlwaysVisible) 1f else collapseProgress.value
                },
            )
        },
        navigationIcon = {
            if (showNavigationIcon) {
                // 头图上白色箭头需要底衬，保证在浅色封面里也能看清
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(scrimColor.copy(alpha = 0.32f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                        tint = Color.White,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = titleColor,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.drawBehind {
            val progress = if (titleAlwaysVisible) 1f else collapseProgress.value
            if (progress > 0f) {
                drawRect(surfaceColor.copy(alpha = progress))
            }
        },
    )
}

@Composable
private fun ArticleLoadingState(safePadding: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        Modifier
            .fillMaxSize()
            .padding(
                start = safePadding.calculateStartPadding(layoutDirection),
                top = safePadding.calculateTopPadding(),
                end = safePadding.calculateEndPadding(layoutDirection),
                bottom = safePadding.calculateBottomPadding(),
            ),
    ) {
        LoadingBox(Modifier.fillMaxSize())
    }
}

@Composable
private fun ArticleErrorState(message: String, safePadding: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        Modifier
            .fillMaxSize()
            .padding(
                start = safePadding.calculateStartPadding(layoutDirection),
                top = safePadding.calculateTopPadding(),
                end = safePadding.calculateEndPadding(layoutDirection),
                bottom = safePadding.calculateBottomPadding(),
            ),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleContent(
    bodyBlocks: List<ArticleBlock>,
    app: MarketAppInfo?,
    hero: @Composable () -> Unit,
    innerPadding: PaddingValues,
    bottomInset: Dp,
    lazyListState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenApp: (MarketAppInfo) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            // 头图铺到状态栏下面，顶部不设内边距；底部避开导航条
            top = 0.dp,
            bottom = bottomInset + PageVerticalPadding,
        ),
    ) {
        item(key = "hero") { hero() }
        itemsIndexed(bodyBlocks, key = { index, _ -> "block-$index" }) { index, block ->
            ArticleBlockContent(
                block = block,
                onOpenApp = onOpenApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(
                        top = when {
                            index == 0 -> 24.dp
                            block is ArticleBlock.Heading -> 32.dp
                            else -> 16.dp
                        },
                        bottom = if (index == bodyBlocks.lastIndex) 24.dp else 0.dp,
                    ),
            )
        }
        app?.let { appInfo ->
            item(key = "app") {
                ArticleAppCard(
                    app = appInfo,
                    onOpenApp = onOpenApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = if (bodyBlocks.isEmpty()) 20.dp else 0.dp),
                )
            }
        }
    }
}

/**
 * 全幅编辑式头图：图片铺满、底部渐变压暗，奖项标签 + 标题以白色层叠在图上。
 */
@Composable
private fun ArticleHero(
    imageUrl: String,
    placeholderImageUrl: String,
    awardLabel: String,
    title: String,
    reason: String,
    showTitleLabel: Boolean,
    showCoverText: Boolean,
    accentColor: Color,
    onAccentColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ArticleHeroHeight,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        if (imageUrl.isNotBlank()) {
            AppAsyncImage(
                url = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (!showCoverText) {
                            scaleX = 1.03f
                            scaleY = 1.03f
                        }
                    },
                onDominantColor = onAccentColor,
                placeholderMemoryCacheUrl = placeholderImageUrl.takeIf { it.isNotBlank() && it != imageUrl },
            )
        }
        if (showCoverText) {
            // 先用黑色压暗保证对比度，再叠加取色渐变保留封面色调
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1f to accentColor.copy(alpha = 0.85f),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                if (showTitleLabel) {
                    Text(
                        text = awardLabel,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 34.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (reason.isNotBlank()) {
                    Text(
                        text = reason,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * 多应用合辑头图：封面 + 底部渐变 + 标题 / 摘要 + 应用图标行。
 */
@Composable
private fun GroupArticleHero(
    imageUrl: String,
    placeholderImageUrl: String,
    title: String,
    summary: String,
    apps: List<MarketAppInfo>,
    accentColor: Color,
    onAccentColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(GroupHeroHeight)
            .background(accentColor),
    ) {
        if (imageUrl.isNotBlank()) {
            AppAsyncImage(
                url = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onDominantColor = onAccentColor,
                placeholderMemoryCacheUrl = placeholderImageUrl.takeIf { it.isNotBlank() && it != imageUrl },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to accentColor.copy(alpha = 0.9f),
                        1f to accentColor,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp, top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (summary.isNotBlank()) {
                Text(
                    text = summary,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            AdaptiveAppIconRow(
                apps = apps,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 正文末尾的应用信息卡：图标 + 名称 / 摘要 + 安装（打开详情）按钮。
 */
@Composable
private fun ArticleAppCard(
    app: MarketAppInfo,
    onOpenApp: (MarketAppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    MarketCard(
        modifier = modifier,
        onClick = { onOpenApp(app) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                url = app.icon,
                contentDescription = appDisplayName(app.displayName, app.source),
                size = 52.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appDisplayName(app.displayName, app.source),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.todayAppSummary(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ArticleInstallButton(onClick = { onOpenApp(app) })
        }
    }
}

@Composable
private fun ArticleBlockContent(
    block: ArticleBlock,
    onOpenApp: (MarketAppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (block) {
        is ArticleBlock.Heading -> Text(
            text = block.value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier,
        )

        is ArticleBlock.Paragraph -> Text(
            text = block.value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            modifier = modifier,
        )

        is ArticleBlock.Image -> {
            // 只在服务端给了宽高时占位；富文本里的图没有尺寸，锁死比例会把竖图裁掉大半
            val ratio = block.width.takeIf { it > 0 }
                ?.let { width -> block.height.takeIf { it > 0 }?.let { width.toFloat() / it } }
            AppAsyncImage(
                url = block.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .then(if (ratio != null) Modifier.aspectRatio(ratio) else Modifier)
                    .clip(RoundedCornerShape(20.dp)),
            )
        }

        is ArticleBlock.Apps -> ArticleAppRows(
            apps = block.values,
            onOpenApp = onOpenApp,
            modifier = modifier,
        )
    }
}

@Composable
private fun ArticleAppRows(
    apps: List<MarketAppInfo>,
    onOpenApp: (MarketAppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        RelatedAppsList(
            apps = apps,
            onOpenApp = onOpenApp,
        ) { app ->
            ArticleInstallButton(onClick = { onOpenApp(app) })
        }
    }
}

@Composable
private fun ArticleInstallButton(
    onClick: () -> Unit,
    colors: AppButtonColors = AppButtonDefaults.filled(),
) {
    AppButton(
        text = installActionText(),
        onClick = onClick,
        colors = colors,
        cornerRadius = 100.dp,
        minWidth = 0.dp,
        minHeight = 0.dp,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
    )
}

private sealed interface ArticleBlock {
    data class Heading(val value: String) : ArticleBlock
    data class Paragraph(val value: String) : ArticleBlock
    data class Image(
        val url: String,
        val width: Int = 0,
        val height: Int = 0,
    ) : ArticleBlock

    data class Apps(val values: List<MarketAppInfo>) : ArticleBlock
}

private fun List<ArticleBlock>.withoutBoundaryImages(): List<ArticleBlock> {
    val firstImage = indexOfFirst { it is ArticleBlock.Image }
    if (firstImage < 0) return this
    val lastImage = indexOfLast { it is ArticleBlock.Image }
    return filterIndexed { index, _ -> index != firstImage && index != lastImage }
}

private val imageTag =
    Regex("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", RegexOption.IGNORE_CASE)
private val strongParagraphTag = Regex(
    "<p\\b[^>]*>\\s*<(?:strong|b)\\b[^>]*>(.*?)</(?:strong|b)>\\s*</p>",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
)
private val headingOpenTag = Regex("<h[1-6]\\b[^>]*>", RegexOption.IGNORE_CASE)
private val headingCloseTag = Regex("</h[1-6]\\s*>", RegexOption.IGNORE_CASE)
private val breakTag = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
private val blockBoundaryTag = Regex("</?(?:p|div|li|ul|ol)\\b[^>]*>", RegexOption.IGNORE_CASE)
private val anyTag = Regex("<[^>]+>")
private const val BlockSeparator = "\u0001"
private const val HeadingMarker = "\u0002"
private const val ImageMarker = "\u0003"

private fun orderedArticleBlocks(article: TodayArticle): List<ArticleBlock> {
    val result = mutableListOf<ArticleBlock>()
    val pendingApps = mutableListOf<MarketAppInfo>()
    val featuredPackage = article.apps.singleOrNull()?.packageName
    var skippedHeroBanner = false

    fun flushApps() {
        if (pendingApps.isNotEmpty()) {
            result += ArticleBlock.Apps(pendingApps.toList())
            pendingApps.clear()
        }
    }

    article.blocks.forEach { block ->
        if (block is TodayArticleBlock.App) {
            if (block.value.packageName != featuredPackage) pendingApps += block.value
            return@forEach
        }

        flushApps()
        when (block) {
            is TodayArticleBlock.Banner -> {
                if (!skippedHeroBanner) {
                    skippedHeroBanner = true
                } else if (block.imageUrl.isNotBlank()) {
                    result += ArticleBlock.Image(block.imageUrl, block.width, block.height)
                }
            }

            is TodayArticleBlock.RichText -> result += parseArticleBlocks(block.html)
            is TodayArticleBlock.Image -> if (block.imageUrl.isNotBlank()) {
                result += ArticleBlock.Image(block.imageUrl, block.width, block.height)
            }
        }
    }
    flushApps()
    return result
}

private fun parseArticleBlocks(html: String): List<ArticleBlock> {
    if (html.isBlank()) return emptyList()
    val images = mutableListOf<String>()
    var normalized = strongParagraphTag.replace(html) { match ->
        "$BlockSeparator$HeadingMarker${match.groupValues[1]}$BlockSeparator"
    }
    normalized = imageTag.replace(normalized) { match ->
        val index = images.size
        images += decodeArticleEntities(match.groupValues[1])
        "$BlockSeparator$ImageMarker$index$BlockSeparator"
    }
    normalized = headingOpenTag.replace(normalized, "$BlockSeparator$HeadingMarker")
    normalized = headingCloseTag.replace(normalized, BlockSeparator)
    normalized = breakTag.replace(normalized, "\n")
    normalized = blockBoundaryTag.replace(normalized, BlockSeparator)
    normalized = anyTag.replace(normalized, "")

    val blocks = normalized.split(BlockSeparator).mapNotNull { rawBlock ->
        val value = plainArticleText(rawBlock.removePrefix(HeadingMarker))
        when {
            rawBlock.startsWith(ImageMarker) -> {
                rawBlock.removePrefix(ImageMarker).trim().toIntOrNull()
                    ?.let(images::getOrNull)
                    ?.let { ArticleBlock.Image(it) }
            }

            value.isBlank() -> null
            rawBlock.startsWith(HeadingMarker) || value.looksLikeArticleHeading() -> ArticleBlock.Heading(
                value
            )

            else -> ArticleBlock.Paragraph(value)
        }
    }
    return blocks.ifEmpty {
        plainArticleText(html).takeIf(String::isNotBlank)
            ?.let { listOf(ArticleBlock.Paragraph(it)) }.orEmpty()
    }
}

private fun String.looksLikeArticleHeading(): Boolean =
    length in 2..24 && '\n' !in this && none { it in "。！？!?；;" }

private fun plainArticleText(value: String): String = decodeArticleEntities(value)
    .lines()
    .map(String::trim)
    .filter(String::isNotBlank)
    .joinToString("\n")

private fun decodeArticleEntities(value: String): String = value
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&apos;", "'")
