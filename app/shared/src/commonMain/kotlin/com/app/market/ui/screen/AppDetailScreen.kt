package com.app.market.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.market.AppComment
import com.app.market.domain.model.market.AppDetail
import com.app.market.domain.model.market.AppPromotion
import com.app.market.domain.model.market.AppScreenshot
import com.app.market.domain.model.market.AppSource
import com.app.market.domain.model.market.AppVideo
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.market.ScreenshotOrientation
import com.app.market.domain.model.market.isDownloadBlocked
import com.app.market.domain.model.market.isReservation
import com.app.market.platform.ImageSaveResult
import com.app.market.platform.UiPlatform
import com.app.market.resources.Res
import com.app.market.resources.age_rating
import com.app.market.resources.app_detail
import com.app.market.resources.app_info
import com.app.market.resources.app_update_time
import com.app.market.resources.app_version
import com.app.market.resources.back
import com.app.market.resources.cancel_download
import com.app.market.resources.comments_and_scores
import com.app.market.resources.date_full
import com.app.market.resources.download_count
import com.app.market.resources.featured_offers
import com.app.market.resources.historical_versions
import com.app.market.resources.historical_versions_summary
import com.app.market.resources.ic_star
import com.app.market.resources.ic_star_fill
import com.app.market.resources.image_save_failed
import com.app.market.resources.image_save_unsupported
import com.app.market.resources.image_saved
import com.app.market.resources.introduction
import com.app.market.resources.more_options
import com.app.market.resources.no_other_app_store
import com.app.market.resources.num_comments
import com.app.market.resources.open
import com.app.market.resources.open_in_other_app_store
import com.app.market.resources.open_link_failed
import com.app.market.resources.package_name
import com.app.market.resources.preview
import com.app.market.resources.privacy_policy
import com.app.market.resources.registration_num
import com.app.market.resources.reinstall
import com.app.market.resources.reserve
import com.app.market.resources.same_developer
import com.app.market.resources.save_image
import com.app.market.resources.tap_to_open
import com.app.market.resources.update
import com.app.market.resources.video_play
import com.app.market.resources.view_more
import com.app.market.resources.view_offer
import com.app.market.ui.component.AppActionButton
import com.app.market.ui.component.AppAsyncImage
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppButtonDefaults
import com.app.market.ui.component.AppTextButton
import com.app.market.ui.component.AppVideoViewer
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.PageHorizontalPadding
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.RelatedAppsCard
import com.app.market.ui.component.SectionSpacing
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.model.actionKind
import com.app.market.ui.theme.MarketShapeTokens
import com.app.market.ui.util.LocalStripAppNameSubtitle
import com.app.market.ui.util.allowColorSampling
import com.app.market.ui.util.animatedRect
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.formatCount
import com.app.market.ui.util.formatSize
import com.app.market.ui.util.installActionText
import com.app.market.ui.util.localDateOf
import com.app.market.ui.util.splitAppDisplayName
import com.app.market.ui.util.topRegionLuminance
import com.app.market.viewmodel.AppDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.number
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private val PREVIEW_ITEM_HEIGHT = 200.dp
private val PREVIEW_CORNER = 16.dp
private val PROMOTION_PREVIEW_WIDTH = 256.dp
private const val PORTRAIT_SCREENSHOT_RATIO = 5f / 9f
private const val SCREENSHOT_RATIO_TIMEOUT_MS = 5_000L
private const val SCREENSHOT_RATIO_SAMPLE_PX = 256
private const val SCREENSHOT_RATIO_CONCURRENCY = 3

private const val SCREENSHOT_VIEWER_ANIM_MS = 400
private const val SCREENSHOT_CORNER_DP = 16f
private const val LANDSCAPE_SCREENSHOT_RATIO = 16f / 9f

private const val PROMOTION_EXPANDED_RATIO = 9f / 16f
private val PROMOTION_SHEET_MAX_WIDTH = 420.dp

// 详情页头部：320dp 高的渐变 Hero，主操作与信息胶囊都收在底部
private val HERO_HEIGHT = 320.dp
private val HERO_ICON_SIZE = 96.dp
private const val HERO_ACCENT_ALPHA = 0.38f
private const val HERO_ACCENT_MID_ALPHA = 0.12f
private val HERO_ACTION_MIN_HEIGHT = 52.dp
private val BOTTOM_ACTION_MIN_HEIGHT = 48.dp
private val META_CHIP_PADDING = PaddingValues(horizontal = 10.dp, vertical = 4.dp)

// 顶栏底色与标题随 Hero 滚出而淡入；底栏主操作在 Hero 的操作区滚出后才接管
private const val TOP_BAR_ANIM_MS = 220
private val TOP_BAR_FADE_THRESHOLD = 96.dp
private val BOTTOM_BAR_FADE_THRESHOLD = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    appId: Long,
    packageName: String,
    externalQuery: String? = null,
    source: AppSource = AppSource.XIAOMI,
    onOpenDetail: (MarketAppInfo) -> Unit,
    onOpenHistory: (MarketAppInfo) -> Unit,
    onBack: () -> Unit,
) {
    val uiPlatform = koinInject<UiPlatform>()
    LaunchedEffect(appId, packageName, externalQuery, source) {
        viewModel.load(appId, packageName, externalQuery, source)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates = viewModel.downloadStates.collectAsStateWithLifecycle()
    val showComments by viewModel.showComments.collectAsStateWithLifecycle()
    val showSameDeveloper by viewModel.showSameDeveloper.collectAsStateWithLifecycle()
    val showPromotions by viewModel.showPromotions.collectAsStateWithLifecycle()

    val installText = installActionText()
    val updateText = stringResource(Res.string.update)
    val openText = stringResource(Res.string.open)
    val reserveText = stringResource(Res.string.reserve)
    val backText = stringResource(Res.string.back)
    val current = state.detail
    val currentActionKind = state.actionKind
    val currentPackageName = current?.app?.packageName
    // 进度每变 1% 就是新值：只传 State，顶层仅订阅离散布尔，读取下沉到按钮与菜单
    val currentDownloadState = remember(currentPackageName) {
        derivedStateOf { currentPackageName?.let { downloadStates.value[it] } }
    }
    val canRedownload = current != null && currentActionKind == AppActionKind.OPEN
    val canCancelDownload by remember(currentDownloadState) {
        derivedStateOf { currentDownloadState.value?.isComplete == false }
    }
    val actionKind = currentActionKind ?: current?.app?.actionKind()
    val actionText = when (actionKind) {
        AppActionKind.INSTALL -> installText
        AppActionKind.UPDATE -> updateText
        AppActionKind.OPEN -> openText
        AppActionKind.RESERVE -> reserveText
        null -> ""
    }
    val collapsedTitle = current?.app
        ?.let { appDisplayName(it.displayName, it.source) }
        ?: stringResource(Res.string.app_detail)

    val imageSaveScope = rememberCoroutineScope()
    val savedText = stringResource(Res.string.image_saved)
    val saveFailedText = stringResource(Res.string.image_save_failed)
    val saveUnsupportedText = stringResource(Res.string.image_save_unsupported)
    val noOtherAppStoreText = stringResource(Res.string.no_other_app_store)
    val canOpenOtherAppStore = uiPlatform.packageInstallationSupported
    val openInOtherAppStore: (String) -> Unit = { targetPackageName ->
        if (!uiPlatform.openInOtherAppStore(targetPackageName)) {
            uiPlatform.showToast(noOtherAppStoreText)
        }
    }
    val saveImage: (String, String) -> Unit = { url, fileName ->
        imageSaveScope.launch {
            val message = when (uiPlatform.saveImageToPictures(url, fileName)) {
                ImageSaveResult.Saved -> savedText
                ImageSaveResult.Failed -> saveFailedText
                ImageSaveResult.Unsupported -> saveUnsupportedText
            }
            uiPlatform.showToast(message)
        }
    }
    val phase = when {
        current != null -> DetailPhase.Content
        state.errorMessage.isNotEmpty() -> DetailPhase.Error
        else -> DetailPhase.Loading
    }
    // 主操作与 ⋮ 菜单的回调与可见性集中一处，Hero / 顶栏 / 底栏共用
    val actionState = if (current != null && actionKind != null) {
        DetailActionState(
            packageName = current.app.packageName,
            text = actionText,
            kind = actionKind,
            downloadState = currentDownloadState,
            enabled = actionKind == AppActionKind.OPEN || !current.app.isDownloadBlocked(),
            canOpenOtherAppStore = canOpenOtherAppStore,
            canRedownload = canRedownload,
            canCancelDownload = canCancelDownload,
            onAction = {
                when (actionKind) {
                    AppActionKind.OPEN -> viewModel.openApp(current.app)
                    AppActionKind.INSTALL, AppActionKind.UPDATE -> viewModel.install(current.app)
                    AppActionKind.RESERVE -> Unit
                }
            },
            onResumeDownload = {
                when (actionKind) {
                    AppActionKind.OPEN -> viewModel.redownload(current.app)
                    AppActionKind.INSTALL, AppActionKind.UPDATE -> viewModel.install(current.app)
                    AppActionKind.RESERVE -> Unit
                }
            },
            onOpenOtherAppStore = { openInOtherAppStore(current.app.packageName) },
            onRedownload = { viewModel.redownload(current.app) },
            onClearDownload = { viewModel.clearDownload(current.app.packageName) },
            onInstallDownloaded = viewModel::installDownloaded,
            onCancel = viewModel::cancelDownload,
        )
    } else {
        null
    }
    // 预取截图真实宽高比；只要比例，故限解码尺寸与并发
    val platformContext = LocalPlatformContext.current
    val screenshotRatios = remember(current?.app?.appId) { mutableStateMapOf<String, Float>() }
    LaunchedEffect(current?.screenshots) {
        val screenshots = current?.screenshots ?: return@LaunchedEffect
        val imageLoader = SingletonImageLoader.get(platformContext)
        val slots = Semaphore(SCREENSHOT_RATIO_CONCURRENCY)
        for (shot in screenshots) {
            if (screenshotRatios.containsKey(shot.url)) continue
            launch {
                val image = slots.withPermit {
                    withTimeoutOrNull(SCREENSHOT_RATIO_TIMEOUT_MS.milliseconds) {
                        imageLoader.execute(
                            ImageRequest.Builder(platformContext)
                                .data(shot.url)
                                .size(SCREENSHOT_RATIO_SAMPLE_PX)
                                .build()
                        ).image
                    }
                }
                screenshotRatios[shot.url] = if (image != null && image.width > 0 && image.height > 0) {
                    image.width.toFloat() / image.height
                } else {
                    screenshotAspectRatio(shot.orientation)
                }
            }
        }
    }

    var expandedScreenshot by rememberSaveable(appId) { mutableIntStateOf(-1) }
    val screenshotCardBounds = remember { mutableStateMapOf<String, Rect>() }
    var playingVideo by remember(appId) { mutableStateOf<AppVideo?>(null) }
    val videoCardBounds = remember { mutableStateMapOf<String, Rect>() }

    // 列表滚过 Hero 后顶栏底色淡入、底栏主操作淡出——两者都由列表滚动驱动
    val listState = rememberLazyListState()
    val fadeThresholdPx = with(LocalDensity.current) { TOP_BAR_FADE_THRESHOLD.roundToPx() }
    val bottomBarThresholdPx = with(LocalDensity.current) { BOTTOM_BAR_FADE_THRESHOLD.roundToPx() }
    val scrolledPastHero by remember(listState, fadeThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > fadeThresholdPx
        }
    }
    val scrolledPastHeroActions by remember(listState, bottomBarThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > bottomBarThresholdPx
        }
    }
    val barOpaque = phase != DetailPhase.Content || scrolledPastHero
    val barColor by animateColorAsState(
        targetValue = if (barOpaque) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(TOP_BAR_ANIM_MS),
        label = "detailBarColor",
    )
    val barTitleAlpha by animateFloatAsState(
        targetValue = if (barOpaque) 1f else 0f,
        animationSpec = tween(TOP_BAR_ANIM_MS),
        label = "detailBarTitleAlpha",
    )

    SelectionContainer(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = collapsedTitle,
                                modifier = Modifier.alpha(barTitleAlpha),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = backText,
                                )
                            }
                        },
                        actions = {
                            val menuActions = actionState
                            if (menuActions != null && menuActions.hasMenu) {
                                DetailMoreMenu(
                                    canOpenOtherAppStore = menuActions.canOpenOtherAppStore,
                                    canRedownload = menuActions.canRedownload,
                                    canCancelDownload = menuActions.canCancelDownload,
                                    onOpenOtherAppStore = menuActions.onOpenOtherAppStore,
                                    onRedownload = menuActions.onRedownload,
                                    onClearDownload = menuActions.onClearDownload,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = barColor,
                            scrolledContainerColor = barColor,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                },
                bottomBar = {
                    // Hero 滚出视口后接管主操作，长页面里不必回滚到顶部
                    AnimatedVisibility(
                        visible = phase == DetailPhase.Content &&
                                scrolledPastHeroActions &&
                                actionState != null,
                        enter = fadeIn(tween(TOP_BAR_ANIM_MS)) +
                                slideInVertically(tween(TOP_BAR_ANIM_MS)) { it / 2 },
                        exit = fadeOut(tween(TOP_BAR_ANIM_MS)) +
                                slideOutVertically(tween(TOP_BAR_ANIM_MS)) { it / 2 },
                    ) {
                        val barActions = actionState
                        if (barActions != null) {
                            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(
                                            start = PageHorizontalPadding,
                                            end = PageHorizontalPadding,
                                            top = 10.dp,
                                            bottom = 10.dp,
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    DetailActionButton(
                                        packageName = barActions.packageName,
                                        actionText = barActions.text,
                                        actionKind = barActions.kind,
                                        downloadStateProvider = barActions.downloadState,
                                        enabled = barActions.enabled,
                                        onAction = barActions.onAction,
                                        onResumeDownload = barActions.onResumeDownload,
                                        onInstallDownloaded = barActions.onInstallDownloaded,
                                        onCancel = barActions.onCancel,
                                        minHeight = BOTTOM_ACTION_MIN_HEIGHT,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (barActions.hasMenu) {
                                        DetailMoreMenu(
                                            canOpenOtherAppStore = barActions.canOpenOtherAppStore,
                                            canRedownload = barActions.canRedownload,
                                            canCancelDownload = barActions.canCancelDownload,
                                            onOpenOtherAppStore = barActions.onOpenOtherAppStore,
                                            onRedownload = barActions.onRedownload,
                                            onClearDownload = barActions.onClearDownload,
                                            tonal = true,
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                val layoutDirection = LocalLayoutDirection.current
                // 顶部不加内边距：Hero 要一直画到状态栏下面，顶栏底色再随滚动淡入
                val contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
                )
                Crossfade(
                    targetState = phase,
                    modifier = Modifier.fillMaxSize(),
                    label = "detail",
                ) { target ->
                    when (target) {
                        DetailPhase.Loading -> LoadingBox(Modifier.fillMaxSize())
                        DetailPhase.Error -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                        }

                        DetailPhase.Content -> current?.let { detail ->
                            AppDetailContent(
                                current = detail,
                                actions = actionState,
                                downloadStates = downloadStates,
                                showComments = showComments,
                                showSameDeveloper = showSameDeveloper,
                                showPromotions = showPromotions,
                                contentPadding = contentPadding,
                                listState = listState,
                                viewModel = viewModel,
                                uiPlatform = uiPlatform,
                                saveImage = saveImage,
                                onOpenDetail = onOpenDetail,
                                onOpenHistory = onOpenHistory,
                                screenshotRatios = screenshotRatios,
                                hiddenScreenshot = expandedScreenshot,
                                onExpandScreenshot = { expandedScreenshot = it },
                                onScreenshotCardPositioned = { url, bounds ->
                                    if (bounds == null) screenshotCardBounds.remove(url) else screenshotCardBounds[url] = bounds
                                },
                                onPlayVideo = { playingVideo = it },
                                playingVideoUrl = playingVideo?.url,
                                onVideoCardPositioned = { url, bounds ->
                                    if (bounds == null) videoCardBounds.remove(url) else videoCardBounds[url] = bounds
                                },
                            )
                        }
                    }
                }
            }

            current?.takeIf { expandedScreenshot >= 0 && it.screenshots.isNotEmpty() }?.let { detail ->
                ScreenshotViewer(
                    screenshots = detail.screenshots,
                    screenshotRatios = screenshotRatios,
                    cardBounds = screenshotCardBounds,
                    initialPage = expandedScreenshot.coerceIn(0, detail.screenshots.lastIndex),
                    onPageChange = { expandedScreenshot = it },
                    onClosed = { expandedScreenshot = -1 },
                )
            }

            playingVideo?.let { video ->
                AppVideoViewer(
                    video = video,
                    cardBounds = videoCardBounds[video.url],
                    onClosed = { playingVideo = null },
                )
            }
        }
    }
}

private enum class DetailPhase { Loading, Error, Content }

/** 详情页主操作状态：Hero、顶栏 ⋮ 菜单与底部操作栏共用同一份回调。 */
private class DetailActionState(
    val packageName: String,
    val text: String,
    val kind: AppActionKind,
    val downloadState: State<DownloadState?>,
    val enabled: Boolean,
    val canOpenOtherAppStore: Boolean,
    val canRedownload: Boolean,
    val canCancelDownload: Boolean,
    val onAction: () -> Unit,
    val onResumeDownload: () -> Unit,
    val onOpenOtherAppStore: () -> Unit,
    val onRedownload: () -> Unit,
    val onClearDownload: () -> Unit,
    val onInstallDownloaded: (String) -> Unit,
    val onCancel: (String) -> Unit,
) {
    val hasMenu: Boolean get() = canOpenOtherAppStore || canRedownload || canCancelDownload
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailContent(
    current: AppDetail,
    actions: DetailActionState?,
    downloadStates: State<Map<String, DownloadState>>,
    showComments: Boolean,
    showSameDeveloper: Boolean,
    showPromotions: Boolean,
    contentPadding: PaddingValues,
    listState: LazyListState,
    viewModel: AppDetailViewModel,
    uiPlatform: UiPlatform,
    saveImage: (String, String) -> Unit,
    onOpenDetail: (MarketAppInfo) -> Unit,
    onOpenHistory: (MarketAppInfo) -> Unit,
    screenshotRatios: Map<String, Float>,
    hiddenScreenshot: Int,
    onExpandScreenshot: (Int) -> Unit,
    onScreenshotCardPositioned: (String, Rect?) -> Unit,
    onPlayVideo: (AppVideo) -> Unit,
    playingVideoUrl: String?,
    onVideoCardPositioned: (String, Rect?) -> Unit,
) {
    val installText = installActionText()
    val reserveText = stringResource(Res.string.reserve)
    val previewTitle = stringResource(Res.string.preview)
    val promotionsTitle = stringResource(Res.string.featured_offers)
    val appInfoTitle = stringResource(Res.string.app_info)
    val historicalVersionsLabel = stringResource(Res.string.historical_versions)
    val historicalVersionsSummary = stringResource(Res.string.historical_versions_summary)

    val commentCount = maxOf(current.commentCount, current.comments.size.toLong())
    val commentsLabel = stringResource(
        Res.string.num_comments,
        formatCount(commentCount).ifBlank { "0" }
    )
    val commentsTitle = stringResource(Res.string.comments_and_scores)

    val sameDeveloperTitle = stringResource(Res.string.same_developer)
    val ageLabel = stringResource(Res.string.age_rating)
    val downloadCountLabel = stringResource(Res.string.download_count)
    val registrationLabel = stringResource(Res.string.registration_num)
    val privacyLabel = stringResource(Res.string.privacy_policy)
    val packageNameLabel = stringResource(Res.string.package_name)
    val tapToOpenText = stringResource(Res.string.tap_to_open)
    val viewMoreText = stringResource(Res.string.view_more)
    val versionLabel = stringResource(Res.string.app_version)
    val updateTimeLabel = stringResource(Res.string.app_update_time)
    val introduction = current.introduction.ifBlank { current.brief }
    val stripNameSubtitle = LocalStripAppNameSubtitle.current
    val uriHandler = LocalUriHandler.current
    var showCommentsSheet by rememberSaveable(current.app.appId) { mutableStateOf(false) }
    var showPromotionSheet by rememberSaveable(current.app.appId) { mutableStateOf(false) }
    var promotionSheetIndex by rememberSaveable(current.app.appId) { mutableIntStateOf(0) }
    val sheetPromotion = current.promotions.getOrNull(promotionSheetIndex)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        // Hero 是列表第一项：随内容一起滚出，顶栏底色再淡入
        item(key = "hero") {
            actions?.let { heroActions ->
                DetailHero(
                    detail = current,
                    actions = heroActions,
                    onSaveImage = saveImage,
                )
            }
        }

        item(key = "stats") {
            AppDetailStatsRow(
                rating = formatRating(current.app.ratingScore),
                reviews = commentsLabel,
                downloads = current.downloadCount
                    .takeUnless { current.app.source == AppSource.SAMSUNG }
                    ?.let { formatCount(it).ifBlank { "0" } },
                downloadsLabel = downloadCountLabel,
                ageClassification = current.ageClassification,
                ageLabel = ageLabel,
                onReviewsClick = if (current.comments.isNotEmpty()) {
                    { showCommentsSheet = true }
                } else {
                    null
                },
            )
        }

        val showPromotionsSection = showPromotions && current.promotions.isNotEmpty()
        if (showPromotionsSection || current.videos.isNotEmpty() || current.screenshots.isNotEmpty() || introduction.isNotBlank()) {
            item(key = "preview") {
                PreviewAndIntroductionSection(
                    current = current,
                    showPromotions = showPromotionsSection,
                    promotionsTitle = promotionsTitle,
                    screenshotsTitle = previewTitle,
                    screenshotRatios = screenshotRatios,
                    hiddenScreenshot = hiddenScreenshot,
                    onExpandScreenshot = onExpandScreenshot,
                    onScreenshotCardPositioned = onScreenshotCardPositioned,
                    onPlayVideo = onPlayVideo,
                    playingVideoUrl = playingVideoUrl,
                    onVideoCardPositioned = onVideoCardPositioned,
                    introduction = introduction,
                    onOpenPromotion = {
                        promotionSheetIndex = it
                        showPromotionSheet = true
                    },
                    onSaveImage = saveImage,
                )
            }
        }

        item(key = "appinfo") {
            DetailSection(
                title = appInfoTitle,
            ) {
                MarketCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PageHorizontalPadding),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoLine(packageNameLabel, current.app.packageName)
                        InfoLine(versionLabel, current.app.versionName)
                        if (current.updateTime > 0L) {
                            val date = localDateOf(current.updateTime)
                            InfoLine(
                                updateTimeLabel,
                                stringResource(
                                    Res.string.date_full,
                                    date.year,
                                    date.month.number,
                                    date.day
                                ),
                            )
                        }
                        if (current.registrationNum.isNotBlank()) {
                            InfoLine(registrationLabel, current.registrationNum)
                        }
                        if (current.privacyUrl.isNotBlank()) {
                            InfoLine(
                                label = privacyLabel,
                                value = tapToOpenText,
                                valueColor = MaterialTheme.colorScheme.primary,
                                onValueClick = { uriHandler.openUri(current.privacyUrl) },
                            )
                        }
                        if (current.app.source == AppSource.WANDOUJIA) {
                            InfoLine(
                                label = historicalVersionsLabel,
                                value = historicalVersionsSummary,
                                valueColor = MaterialTheme.colorScheme.primary,
                                onValueClick = { onOpenHistory(current.app) },
                            )
                        }
                    }
                }
            }
        }

        if (showComments && current.comments.isNotEmpty()) {
            item(key = "comments") {
                val displayedComments = current.comments.take(2)
                DetailSection(
                    title = commentsTitle,
                ) {
                    MarketCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PageHorizontalPadding),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            RatingSummary(
                                score = formatRating(current.app.ratingScore),
                                commentsLabel = commentsLabel,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                displayedComments.forEach { comment ->
                                    CommentContent(comment)
                                }
                            }
                            if (current.comments.size > 2) {
                                AppTextButton(
                                    text = viewMoreText,
                                    onClick = { showCommentsSheet = true },
                                    minWidth = 0.dp,
                                    minHeight = 0.dp,
                                    insideMargin = PaddingValues(0.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        val sameDeveloper = current.sameDeveloperApps
            .filter {
                it.appId != current.app.appId &&
                        !it.packageName.equals(current.app.packageName, ignoreCase = true)
            }
            .take(3)
        if (showSameDeveloper && sameDeveloper.isNotEmpty()) {
            item(key = "samedev") {
                DetailSection(
                    title = sameDeveloperTitle,
                ) {
                    Box(Modifier.padding(horizontal = PageHorizontalPadding)) {
                        RelatedAppsCard(
                            apps = sameDeveloper,
                            onOpenApp = onOpenDetail,
                            title = { app -> app.displayName.splitAppDisplayName(app.source).appName },
                            summary = { app ->
                                (if (stripNameSubtitle) "" else app.displayName.splitAppDisplayName(app.source).description)
                                    .ifBlank { app.publisherName }
                            },
                        ) { app ->
                            val reservation = app.isReservation()
                            val rowDownloadState = remember(app.packageName) {
                                derivedStateOf { downloadStates.value[app.packageName] }
                            }
                            DetailActionButton(
                                packageName = app.packageName,
                                actionText = if (reservation) reserveText else installText,
                                actionKind = if (reservation) AppActionKind.RESERVE else AppActionKind.INSTALL,
                                downloadStateProvider = rowDownloadState,
                                onAction = { viewModel.install(app) },
                                onResumeDownload = { viewModel.install(app) },
                                onInstallDownloaded = viewModel::installDownloaded,
                                onCancel = viewModel::cancelDownload,
                                minHeight = 34.dp,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCommentsSheet) {
        val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            sheetState = commentsSheetState,
        ) {
            Text(
                text = commentsTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PageHorizontalPadding)
                    .padding(bottom = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = PageHorizontalPadding,
                    end = PageHorizontalPadding,
                    bottom = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "comments-summary") {
                    RatingSummary(
                        score = formatRating(current.app.ratingScore),
                        commentsLabel = commentsLabel,
                    )
                }
                itemsIndexed(
                    items = current.comments,
                    key = { index, comment -> "${index}_${comment.userName}" },
                ) { _, comment ->
                    CommentContent(comment)
                }
            }
        }
    }

    if (showPromotionSheet && sheetPromotion != null) {
        val promotionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPromotionSheet = false },
            sheetState = promotionSheetState,
            sheetMaxWidth = PROMOTION_SHEET_MAX_WIDTH,
            // 图片本身就是面板背景，默认把手会露出面板底色
            dragHandle = null,
            containerColor = Color.Black,
            contentColor = Color.White,
        ) {
            val openLinkFailedText = stringResource(Res.string.open_link_failed)
            sheetPromotion?.let { promotion ->
                PromotionDetails(
                    promotion = promotion,
                    onOpenOffer = promotion.jumpUrl.takeIf(String::isNotBlank)?.let { jumpUrl ->
                        {
                            runCatching { uriHandler.openUri(jumpUrl) }
                                .onFailure { uiPlatform.showToast(openLinkFailedText) }
                        }
                    },
                )
            }
        }
    }
}

/** 头部 Hero：图标主色渐变 + 图标 / 名称 / 副标题 + 信息胶囊 + 主操作。 */
@Composable
private fun DetailHero(
    detail: AppDetail,
    actions: DetailActionState,
    onSaveImage: (String, String) -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    val fallbackAccent = MaterialTheme.colorScheme.primary
    var accentColor by remember(detail.app.appId) { mutableStateOf(fallbackAccent) }
    val chips = buildList {
        detail.app.versionName.takeIf { it.isNotBlank() }?.let { add(HeroChip(it)) }
        formatSize(detail.app.apkSize).takeIf { it.isNotBlank() }?.let { add(HeroChip(it)) }
        if (detail.category.isNotBlank()) {
            add(HeroChip(detail.category))
        } else if (detail.app.ratingScore > 0.0) {
            add(HeroChip(formatRating(detail.app.ratingScore), showStar = true))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .background(
                Brush.verticalGradient(
                    0f to accentColor.copy(alpha = HERO_ACCENT_ALPHA),
                    0.55f to accentColor.copy(alpha = HERO_ACCENT_MID_ALPHA),
                    1f to surface,
                ),
            ),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PageHorizontalPadding)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SaveableImageBox(
                    url = detail.app.icon,
                    fileName = "${detail.app.packageName}_icon",
                    onSaveImage = onSaveImage,
                ) {
                    AppAsyncImage(
                        url = detail.app.icon,
                        contentDescription = appDisplayName(detail.app.displayName, detail.app.source),
                        modifier = Modifier
                            .size(HERO_ICON_SIZE)
                            .clip(RoundedCornerShape(MarketShapeTokens.CardCorner))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        onDominantColor = { accentColor = it },
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = appDisplayName(detail.app.displayName, detail.app.source),
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = detail.app.publisherName.ifBlank { detail.app.packageName },
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            }
            if (chips.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    chips.forEach { chip ->
                        MetaChip(text = chip.text, showStar = chip.showStar)
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailActionButton(
                    packageName = actions.packageName,
                    actionText = actions.text,
                    actionKind = actions.kind,
                    downloadStateProvider = actions.downloadState,
                    enabled = actions.enabled,
                    onAction = actions.onAction,
                    onResumeDownload = actions.onResumeDownload,
                    onInstallDownloaded = actions.onInstallDownloaded,
                    onCancel = actions.onCancel,
                    minHeight = HERO_ACTION_MIN_HEIGHT,
                    insideMargin = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1f),
                )
                if (actions.hasMenu) {
                    DetailMoreMenu(
                        canOpenOtherAppStore = actions.canOpenOtherAppStore,
                        canRedownload = actions.canRedownload,
                        canCancelDownload = actions.canCancelDownload,
                        onOpenOtherAppStore = actions.onOpenOtherAppStore,
                        onRedownload = actions.onRedownload,
                        onClearDownload = actions.onClearDownload,
                        tonal = true,
                    )
                }
            }
        }
    }
}

private data class HeroChip(
    val text: String,
    val showStar: Boolean = false,
)

/** Hero 里的信息胶囊（版本 / 大小 / 分类 / 评分）。 */
@Composable
private fun MetaChip(
    text: String,
    showStar: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(MarketShapeTokens.TagCorner))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(META_CHIP_PADDING),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showStar) {
            Icon(
                painter = painterResource(Res.drawable.ic_star_fill),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun AppDetailStatsRow(
    rating: String,
    reviews: String,
    downloads: String?,
    downloadsLabel: String,
    /** 空串表示没有分级信息，整格连同分隔线一起省掉。 */
    ageClassification: String,
    ageLabel: String,
    /** 无评论正文时传 null，否则点开只是空面板。 */
    onReviewsClick: (() -> Unit)?,
) {
    MarketCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PageHorizontalPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailStat(
                value = rating,
                label = reviews,
                showStar = true,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onReviewsClick != null) {
                            Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onReviewsClick,
                            )
                        } else {
                            Modifier
                        }
                    ),
            )
            if (downloads != null) {
                DetailStatDivider()
                DetailStat(
                    value = downloads,
                    label = downloadsLabel,
                    modifier = Modifier.weight(1f),
                )
            }
            if (ageClassification.isNotBlank()) {
                DetailStatDivider()
                DetailStat(
                    value = ageClassification,
                    label = ageLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DetailStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    showStar: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showStar) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star_fill),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun PreviewAndIntroductionSection(
    current: AppDetail,
    showPromotions: Boolean,
    promotionsTitle: String,
    screenshotsTitle: String,
    screenshotRatios: Map<String, Float>,
    hiddenScreenshot: Int,
    onExpandScreenshot: (Int) -> Unit,
    onScreenshotCardPositioned: (String, Rect?) -> Unit,
    onPlayVideo: (AppVideo) -> Unit,
    playingVideoUrl: String?,
    onVideoCardPositioned: (String, Rect?) -> Unit,
    introduction: String,
    onOpenPromotion: (Int) -> Unit,
    onSaveImage: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        if (showPromotions) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // 标题自带 8dp 底边距，子区块之间只靠外层 20dp 间距
                SectionTitle(text = promotionsTitle, topPadding = 0.dp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = PageHorizontalPadding),
                ) {
                    itemsIndexed(
                        current.promotions,
                        key = { _, promotion -> promotion.previewImageUrl },
                    ) { index, promotion ->
                        PromotionPreview(
                            promotion = promotion,
                            fileName = "${current.app.packageName}_promotion_${index + 1}",
                            onClick = { onOpenPromotion(index) },
                            onSaveImage = onSaveImage,
                        )
                    }
                }
            }
        }

        if (current.videos.isNotEmpty() || current.screenshots.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionTitle(text = screenshotsTitle, topPadding = 0.dp)
                val readyPrefix = current.screenshots
                    .indexOfFirst { !screenshotRatios.containsKey(it.url) }
                    .let { if (it < 0) current.screenshots.size else it }
                LazyRow(
                    modifier = Modifier.height(PREVIEW_ITEM_HEIGHT),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = PageHorizontalPadding),
                ) {
                    itemsIndexed(
                        current.videos,
                        key = { _, video -> video.url },
                    ) { _, video ->
                        VideoPreviewCard(
                            video = video,
                            hidden = video.url == playingVideoUrl,
                            onClick = { onPlayVideo(video) },
                            onPositioned = { bounds -> onVideoCardPositioned(video.url, bounds) },
                        )
                    }
                    itemsIndexed(
                        current.screenshots.take(readyPrefix),
                        key = { _, shot -> shot.url }) { index, shot ->
                        SaveableImageBox(
                            url = shot.expandedUrl,
                            fileName = "${current.app.packageName}_screenshot_${index + 1}",
                            onClick = { onExpandScreenshot(index) },
                            onSaveImage = onSaveImage,
                        ) {
                            // 滚出视口即移除记录，收回动画据此判断目标是否在场
                            DisposableEffect(shot.url) {
                                onDispose { onScreenshotCardPositioned(shot.url, null) }
                            }
                            Screenshot(
                                screenshot = shot,
                                ratio = screenshotRatios.getValue(shot.url),
                                modifier = Modifier
                                    // positionInRoot 不受父容器裁剪影响；boundsInRoot 会把半可见卡片裁成半张
                                    .onGloballyPositioned {
                                        onScreenshotCardPositioned(shot.url, Rect(it.positionInRoot(), it.size.toSize()))
                                    }
                                    .graphicsLayer { alpha = if (index == hiddenScreenshot) 0f else 1f }
                                    .clip(RoundedCornerShape(PREVIEW_CORNER)),
                            )
                        }
                    }
                }
            }
        }

        if (introduction.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SectionTitle(text = stringResource(Res.string.introduction), topPadding = 0.dp)
                IntroductionSummary(
                    text = introduction,
                )
            }
        }
    }
}

@Composable
private fun VideoPreviewCard(
    video: AppVideo,
    hidden: Boolean,
    onClick: () -> Unit,
    onPositioned: (Rect?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(video.url) {
        onDispose { onPositioned(null) }
    }
    Box(
        modifier = modifier
            .height(PREVIEW_ITEM_HEIGHT)
            .aspectRatio(screenshotAspectRatio(video.orientation))
            .onGloballyPositioned {
                onPositioned(Rect(it.positionInRoot(), it.size.toSize()))
            }
            .graphicsLayer { alpha = if (hidden) 0f else 1f }
            .clip(RoundedCornerShape(PREVIEW_CORNER))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppAsyncImage(
            url = video.coverUrl,
            contentDescription = video.title.takeIf { it.isNotBlank() },
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(Res.string.video_play),
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(24.dp),
            )
        }
    }
}

@Composable
private fun PromotionPreview(
    promotion: AppPromotion,
    fileName: String,
    onClick: () -> Unit,
    onSaveImage: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(PROMOTION_PREVIEW_WIDTH)
            .padding(vertical = 4.dp),
    ) {
        SaveableImageBox(
            url = promotion.previewImageUrl,
            fileName = fileName,
            onClick = onClick,
            onSaveImage = onSaveImage,
        ) {
            AppAsyncImage(
                url = promotion.previewImageUrl,
                contentDescription = promotion.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .width(PROMOTION_PREVIEW_WIDTH)
                    .height(PROMOTION_PREVIEW_WIDTH / LANDSCAPE_SCREENSHOT_RATIO)
                    .clip(RoundedCornerShape(PREVIEW_CORNER)),
            )
        }
        if (promotion.title.isNotBlank()) {
            Text(
                text = promotion.title,
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (promotion.category.isNotBlank()) {
            Text(
                text = promotion.category,
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PromotionDetails(
    promotion: AppPromotion,
    onOpenOffer: (() -> Unit)?,
) {
    var imageRatio by remember(promotion.expandedImageUrl) {
        mutableFloatStateOf(
            PROMOTION_EXPANDED_RATIO
        )
    }
    // 把手压在图片上，颜色按图片顶部亮度取反，浅色图用深色把手
    var handleColor by remember(promotion.expandedImageUrl) { mutableStateOf(Color.White) }
    val sheetScrollState = rememberScrollState()
    val platformContext = LocalPlatformContext.current
    val background = rememberAsyncImagePainter(
        model = remember(promotion.expandedImageUrl, platformContext) {
            ImageRequest.Builder(platformContext)
                .data(promotion.expandedImageUrl)
                .allowColorSampling()
                .build()
        },
        contentScale = ContentScale.Crop,
    )
    LaunchedEffect(background) {
        background.state.collect { state ->
            if (state is AsyncImagePainter.State.Success) {
                val image = state.result.image
                if (image.width > 0 && image.height > 0) {
                    imageRatio = image.width.toFloat() / image.height
                }
                // 亮度采样要做整图位图拷贝
                val luminance = withContext(Dispatchers.Default) { topRegionLuminance(image) }
                if (luminance != null) {
                    handleColor = if (luminance > 0.5f) Color.Black else Color.White
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 背景画在内容内以随 sheet 位移
            .drawBehind { drawPromotionBackground(background) }
            .animateContentSize(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .verticalScroll(sheetScrollState),
        ) {
            // 按背景图比例撑起 sheet 高度
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageRatio),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.4f to Color.Black.copy(alpha = 0.55f),
                            1f to Color.Black.copy(alpha = 0.8f),
                        ),
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 叠图文字固定白色系，不随主题（特例）
                if (promotion.title.isNotBlank()) {
                    Text(
                        text = promotion.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                val labels =
                    listOf(promotion.category, promotion.activityTag).filter(String::isNotBlank)
                if (labels.isNotEmpty()) {
                    Text(
                        text = labels.joinToString(" | "),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (promotion.description.isNotBlank()) {
                    SelectionContainer(
                        modifier = Modifier
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = promotion.description,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        )
                    }
                }
                if (onOpenOffer != null) {
                    AppButton(
                        text = stringResource(Res.string.view_offer),
                        onClick = onOpenOffer,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp),
                        colors = AppButtonDefaults.filled(),
                        cornerRadius = MarketShapeTokens.ButtonCorner,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .width(32.dp)
                .height(4.dp)
                .background(handleColor.copy(alpha = 0.75f), RoundedCornerShape(2.dp)),
        )
    }
}

// 等比铺满内容区（Crop）；painter 未加载时无固有尺寸（NaN 比较为 false），走 else 分支不可见
private fun DrawScope.drawPromotionBackground(painter: Painter) {
    val target = Size(size.width, size.height)
    val intrinsic = painter.intrinsicSize
    if (intrinsic.width > 0f && intrinsic.height > 0f) {
        val scale = maxOf(target.width / intrinsic.width, target.height / intrinsic.height)
        val drawSize = Size(intrinsic.width * scale, intrinsic.height * scale)
        translate(
            left = (target.width - drawSize.width) / 2f,
            top = (target.height - drawSize.height) / 2f,
        ) {
            with(painter) { draw(drawSize) }
        }
    } else {
        with(painter) { draw(target) }
    }
}

@Composable
private fun IntroductionSummary(
    text: String,
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var hasVisualOverflow by remember(text) { mutableStateOf(false) }
    val showMore = !expanded && hasVisualOverflow
    val descriptionStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    Box(
        modifier = Modifier
            .padding(horizontal = PageHorizontalPadding)
            .fillMaxWidth(),
    ) {
        MarketCard(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = descriptionStyle,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (!expanded) hasVisualOverflow = result.hasVisualOverflow
                    },
                )
                if (showMore) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .height(IntrinsicSize.Min)
                            .clickable { expanded = true },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .width(58.dp)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        0f to Color.Transparent,
                                        1f to cardColor,
                                    ),
                                ),
                        )
                        Text(
                            text = stringResource(Res.string.view_more),
                            modifier = Modifier.background(cardColor),
                            color = MaterialTheme.colorScheme.primary,
                            style = descriptionStyle,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 标题自带 20dp 上边距（列表间距已提供，这里用 0）与 8dp 下边距
        SectionTitle(text = title, topPadding = 0.dp)
        content()
    }
}

/** 顶栏 / Hero / 底栏共用的 ⋮ 菜单按钮，弹窗锚定在按钮处。 */
@Composable
private fun DetailMoreMenu(
    canOpenOtherAppStore: Boolean,
    canRedownload: Boolean,
    canCancelDownload: Boolean,
    onOpenOtherAppStore: () -> Unit,
    onRedownload: () -> Unit,
    onClearDownload: () -> Unit,
    modifier: Modifier = Modifier,
    /** Hero 与底栏用填充色调按钮，顶栏用普通图标按钮。 */
    tonal: Boolean = false,
) {
    var showMenu by remember { mutableStateOf(false) }
    val moreText = stringResource(Res.string.more_options)
    Box(modifier) {
        val toggle: () -> Unit = { showMenu = true }
        if (tonal) {
            FilledTonalIconButton(
                onClick = toggle,
                modifier = Modifier.size(HERO_ACTION_MIN_HEIGHT),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = moreText,
                )
            }
        } else {
            IconButton(onClick = toggle) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = moreText,
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            if (canOpenOtherAppStore) {
                val label = stringResource(Res.string.open_in_other_app_store)
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        showMenu = false
                        onOpenOtherAppStore()
                    },
                )
            }
            if (canRedownload) {
                val label = stringResource(Res.string.reinstall)
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        showMenu = false
                        onRedownload()
                    },
                )
            }
            if (canCancelDownload) {
                val label = stringResource(Res.string.cancel_download)
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        showMenu = false
                        onClearDownload()
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    packageName: String,
    actionText: String,
    actionKind: AppActionKind,
    downloadStateProvider: State<DownloadState?>,
    enabled: Boolean = true,
    onAction: () -> Unit,
    onResumeDownload: () -> Unit,
    onInstallDownloaded: (String) -> Unit,
    onCancel: (String) -> Unit,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    insideMargin: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
) {
    val downloadState = downloadStateProvider.value
    if (downloadState == null) {
        AppButton(
            text = actionText,
            onClick = onAction,
            enabled = enabled && actionKind != AppActionKind.RESERVE,
            colors = if (actionKind == AppActionKind.OPEN || actionKind == AppActionKind.RESERVE) {
                AppButtonDefaults.neutral()
            } else {
                AppButtonDefaults.filled()
            },
            cornerRadius = MarketShapeTokens.ButtonCorner,
            minHeight = minHeight,
            insideMargin = insideMargin,
            modifier = modifier,
        )
    } else {
        AppActionButton(
            packageName = packageName,
            actionText = actionText,
            actionKind = actionKind,
            downloadState = downloadState,
            enabled = enabled && actionKind != AppActionKind.RESERVE,
            onAction = onAction,
            onResumeDownload = onResumeDownload,
            onInstallDownloaded = onInstallDownloaded,
            onCancel = onCancel,
            minHeight = minHeight,
            insideMargin = insideMargin,
            modifier = modifier,
        )
    }
}

private fun Modifier.saveImageOnLongPress(
    url: String,
    fileName: String,
    onClick: (() -> Unit)? = null,
    onSave: (String, String) -> Unit,
): Modifier = if (onClick == null) {
    pointerInput(url, fileName) {
        detectTapGestures(onLongPress = { onSave(url, fileName) })
    }
} else {
    combinedClickable(
        onClick = onClick,
        onLongClick = { onSave(url, fileName) },
    )
}

@Composable
private fun SaveableImageBox(
    url: String,
    fileName: String,
    onClick: (() -> Unit)? = null,
    onSaveImage: (String, String) -> Unit,
    content: @Composable () -> Unit,
) {
    var showMenu by remember(url, fileName) { mutableStateOf(false) }
    val saveText = stringResource(Res.string.save_image)
    Box(
        modifier = Modifier.saveImageOnLongPress(
            url = url,
            fileName = fileName,
            onClick = onClick,
            onSave = { _, _ -> showMenu = true },
        ),
    ) {
        content()
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = saveText) },
                onClick = {
                    showMenu = false
                    onSaveImage(url, fileName)
                },
            )
        }
    }
}

@Composable
private fun Screenshot(
    screenshot: AppScreenshot,
    ratio: Float,
    modifier: Modifier = Modifier,
) {
    AppAsyncImage(
        url = screenshot.url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .height(PREVIEW_ITEM_HEIGHT)
            .aspectRatio(ratio),
    )
}

// 全屏截图查看层：变换阶段绘制独立图层，浏览阶段是纯 Pager，二者在进度端点处像素对齐无缝切换
@Composable
private fun ScreenshotViewer(
    screenshots: List<AppScreenshot>,
    screenshotRatios: Map<String, Float>,
    cardBounds: Map<String, Rect>,
    initialPage: Int,
    onPageChange: (Int) -> Unit,
    onClosed: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { screenshots.size }
    val currentOnPageChange by rememberUpdatedState(onPageChange)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentOnPageChange(it) }
    }

    // 0 = 卡片位置，1 = 全屏
    val progress = remember { Animatable(0f) }
    var browsing by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SCREENSHOT_VIEWER_ANIM_MS))
        browsing = true
    }
    val animationScope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        if (!closing) {
            closing = true
            browsing = false
            animationScope.launch {
                progress.animateTo(0f, tween(SCREENSHOT_VIEWER_ANIM_MS))
                onClosed()
            }
        }
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = !closing,
        onBackCompleted = dismiss,
    )

    // 卡片位置以根坐标系上报，需换算到查看层自身坐标系（宽屏侧栏等场景下两者原点不同）
    var viewerOrigin by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { viewerOrigin = it.positionInRoot() }
            .pointerInput(Unit) { detectTapGestures { dismiss() } },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .background(Color.Black),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (browsing) 1f else 0f },
            userScrollEnabled = browsing,
        ) { page ->
            val shot = screenshots[page]
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppAsyncImage(
                    url = shot.expandedUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    placeholderMemoryCacheUrl = shot.url,
                    modifier = Modifier.aspectRatio(
                        screenshotRatios[shot.url] ?: screenshotAspectRatio(shot.orientation)
                    ),
                )
            }
        }

        if (!browsing) {
            val shot = screenshots[pagerState.currentPage]
            val ratio = screenshotRatios[shot.url] ?: screenshotAspectRatio(shot.orientation)
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()
            val fitWidth = minOf(containerWidth, containerHeight * ratio)
            val fitRect = Rect(
                Offset((containerWidth - fitWidth) / 2f, (containerHeight - fitWidth / ratio) / 2f),
                Size(fitWidth, fitWidth / ratio),
            )
            val cardRect = cardBounds[shot.url]?.translate(-viewerOrigin)
                ?: offscreenCardRect(screenshots, pagerState.currentPage, cardBounds, containerWidth, viewerOrigin)
                ?: fitRect
            // 圆角量化：每次半径变化都要重建形状
            val cornerDp by remember(progress) {
                derivedStateOf { (SCREENSHOT_CORNER_DP * (1f - progress.value)).roundToInt() }
            }
            AppAsyncImage(
                url = shot.expandedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholderMemoryCacheUrl = shot.url,
                modifier = Modifier
                    .animatedRect { lerp(cardRect, fitRect, progress.value) }
                    .clip(RoundedCornerShape(cornerDp.dp)),
            )
        }
    }
}

// 目标卡片已滚出视口时的收回终点：按最近已知卡片推算，飞向对应一侧屏幕外
private fun offscreenCardRect(
    screenshots: List<AppScreenshot>,
    targetIndex: Int,
    cardBounds: Map<String, Rect>,
    containerWidth: Float,
    viewerOrigin: Offset,
): Rect? {
    val known = screenshots.withIndex().mapNotNull { (index, shot) ->
        cardBounds[shot.url]?.let { index to it.translate(-viewerOrigin) }
    }
    val nearest = known.minByOrNull { abs(it.first - targetIndex) } ?: return null
    val reference = nearest.second
    val left = if (targetIndex > nearest.first) containerWidth else -reference.width
    return Rect(Offset(left, reference.top), Size(reference.width, reference.height))
}

internal fun screenshotAspectRatio(orientation: ScreenshotOrientation): Float =
    when (orientation) {
        ScreenshotOrientation.PORTRAIT -> PORTRAIT_SCREENSHOT_RATIO
        ScreenshotOrientation.LANDSCAPE -> LANDSCAPE_SCREENSHOT_RATIO
    }

@Composable
private fun CommentContent(comment: AppComment) {
    val body = remember(comment.content) {
        comment.content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CommentHeader(comment)
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        )
    }
}

@Composable
private fun CommentHeader(comment: AppComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = comment.userName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (comment.score > 0) {
            Row {
                // 无 key 的 remember 在 item 位置复用时会保留上一条评论的评分
                val filled = remember(comment.score) { comment.score.roundToInt().coerceIn(1, 5) }
                repeat(5) { index ->
                    if (index < filled) {
                        Icon(
                            painter = painterResource(
                                Res.drawable.ic_star_fill
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(
                                Res.drawable.ic_star
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/** 评分 + 评论数摘要：详情页评论区与评论面板共用。 */
@Composable
private fun RatingSummary(
    score: String,
    commentsLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = score,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = commentsLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onValueClick: (() -> Unit)? = null,
) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onValueClick != null) {
                        Modifier.clickable(
                            onClick = onValueClick,
                            indication = null,
                            interactionSource = null,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.TopEnd,
        ) {
            Text(
                text = value,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                color = valueColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

private fun formatRating(score: Double): String =
    ((score * 10).roundToInt() / 10.0).toString()
