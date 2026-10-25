package com.app.market.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.market.isDownloadBlocked
import com.app.market.resources.Res
import com.app.market.resources.collapse_change_log
import com.app.market.resources.expand_change_log
import com.app.market.resources.ignore_once
import com.app.market.resources.ignore_permanent
import com.app.market.resources.no_change_log
import com.app.market.resources.search_ad_tag
import com.app.market.resources.search_quickapp_tag
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.theme.MarketShapeTokens
import com.app.market.ui.util.allowColorSampling
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.appSourceLabel
import com.app.market.ui.util.dominantImageColor
import com.app.market.ui.util.formatSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import coil3.Image as CoilImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    colorFilter: androidx.compose.ui.graphics.ColorFilter? = null,
    onDominantColor: ((Color) -> Unit)? = null,
    onLoaded: ((CoilImage) -> Unit)? = null,
    placeholderMemoryCacheUrl: String? = null,
    showLoadingIndicator: Boolean = false,
    loadingIndicatorAlignment: Alignment = Alignment.Center,
    loadingIndicatorColor: Color = Color.Gray,
) {
    val platformContext = LocalPlatformContext.current
    val model = remember(url, platformContext, onDominantColor != null, placeholderMemoryCacheUrl) {
        if (onDominantColor == null && placeholderMemoryCacheUrl == null) {
            url
        } else {
            ImageRequest.Builder(platformContext)
                .data(url)
                .apply {
                    if (onDominantColor != null) allowColorSampling()
                    if (placeholderMemoryCacheUrl != null) placeholderMemoryCacheKey(placeholderMemoryCacheUrl)
                }
                .build()
        }
    }
    var loading by remember(url) { mutableStateOf(true) }
    // onSuccess 在主线程，而取色要做整图位图拷贝。
    var sampledImage by remember(url) { mutableStateOf<CoilImage?>(null) }
    val currentOnDominantColor by rememberUpdatedState(onDominantColor)
    val currentOnLoaded by rememberUpdatedState(onLoaded)
    LaunchedEffect(sampledImage) {
        val source = sampledImage ?: return@LaunchedEffect
        val color = withContext(Dispatchers.Default) { dominantImageColor(source) }
        color?.let { currentOnDominantColor?.invoke(it) }
    }
    val image = @Composable { imageModifier: Modifier ->
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
            onSuccess = { state ->
                if (onDominantColor != null) sampledImage = state.result.image
                currentOnLoaded?.invoke(state.result.image)
                loading = false
            },
            onError = { loading = false },
        )
    }
    if (showLoadingIndicator) {
        Box(modifier = modifier, contentAlignment = loadingIndicatorAlignment) {
            image(Modifier.matchParentSize())
            if (loading) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    color = loadingIndicatorColor,
                )
            }
        }
    } else {
        image(modifier)
    }
}

@Composable
fun AppIcon(
    url: String,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    AppAsyncImage(
        url = url,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(MarketShapeTokens.IconCorner)),
    )
}

@Composable
fun RelatedAppsList(
    apps: List<MarketAppInfo>,
    onOpenApp: (MarketAppInfo) -> Unit,
    title: (MarketAppInfo) -> String = { app -> app.displayName },
    summary: (MarketAppInfo) -> String = { app ->
        app.publisherName.ifBlank { app.packageName }
    },
    action: @Composable (MarketAppInfo) -> Unit,
) {
    RelatedAppsCard(
        apps = apps,
        onOpenApp = onOpenApp,
        title = title,
        summary = summary,
        action = action,
    )
}

@Composable
fun RelatedAppsCard(
    apps: List<MarketAppInfo>,
    onOpenApp: (MarketAppInfo) -> Unit,
    title: (MarketAppInfo) -> String = { app -> app.displayName },
    summary: (MarketAppInfo) -> String,
    action: @Composable (MarketAppInfo) -> Unit,
) {
    MarketCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            apps.forEach { app ->
                RelatedAppRow(
                    app = app,
                    title = title(app),
                    summary = summary(app),
                    onOpenApp = { onOpenApp(app) },
                    action = { action(app) },
                )
            }
        }
    }
}

@Composable
fun RelatedAppRow(
    app: MarketAppInfo,
    title: String = app.displayName,
    summary: String,
    onOpenApp: () -> Unit,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenApp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            url = app.icon,
            contentDescription = appDisplayName(app.displayName, app.source),
            size = 48.dp,
        )
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .widthIn(max = 195.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = appDisplayName(title, app.source),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        action()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    // 默认只填满宽度（高度自适应）；在 LazyColumn item 里传 Modifier.fillParentMaxSize() 可真正居中。
    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        LoadingIndicator(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private const val CollapsedChangeLogMaxLines = 1

@Composable
fun AppRow(
    app: MarketAppInfo,
    modifier: Modifier = Modifier,
    actionText: String,
    actionKind: AppActionKind,
    downloadState: DownloadState?,
    onOpenDetail: () -> Unit,
    onAction: () -> Unit,
    onResumeDownload: () -> Unit = onAction,
    onInstallDownloaded: (String) -> Unit,
    onCancel: (String) -> Unit,
    onIgnoreOnce: (() -> Unit)? = null,
    onIgnorePermanent: (() -> Unit)? = null,
    showUpdateLog: Boolean = false,
    showSearchTags: Boolean = false,
    /** 单源没有歧义，只在多源时标来源。 */
    showSourceTag: Boolean = false,
    initialChangeLogExpanded: Boolean = false,
    grouped: Boolean = false,
) {
    val hasMenu = onIgnoreOnce != null && onIgnorePermanent != null
    val showMenu = remember { mutableStateOf(false) }
    var menuMounted by remember { mutableStateOf(false) }
    val onRowLongPress = if (hasMenu) {
        {
            menuMounted = true
            showMenu.value = true
        }
    } else null
    var changeLogExpanded by rememberSaveable(app.packageName, app.versionCode) {
        mutableStateOf(initialChangeLogExpanded)
    }
    val noChangeLogText = stringResource(Res.string.no_change_log)
    val versionSizeText = remember(app) { appVersionSizeText(app) }
    val spSize = with(LocalDensity.current) { 1.sp.toDp() }

    val content: @Composable () -> Unit = {
        Box(
            modifier = if (showUpdateLog && !changeLogExpanded) {
                Modifier.height(58 * spSize)
            } else {
                Modifier
            },
        ) {
            if (hasMenu && menuMounted) {
                IgnoreUpdateMenu(
                    show = showMenu.value,
                    onDismiss = { showMenu.value = false },
                    onIgnoreOnce = {
                        showMenu.value = false
                        onIgnoreOnce.invoke()
                    },
                    onIgnorePermanent = {
                        showMenu.value = false
                        onIgnorePermanent.invoke()
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (grouped) 8.dp else 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                AppIcon(
                    url = app.icon,
                    contentDescription = appDisplayName(app.displayName, app.source),
                    size = if (grouped) 48.dp else 52 * spSize,
                )
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = appDisplayName(app.displayName, app.source),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (showSourceTag) {
                            SearchTag(
                                text = appSourceLabel(app.source),
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                        if (showSearchTags && app.isAd) {
                            SearchTag(
                                text = stringResource(Res.string.search_ad_tag),
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                        if (showSearchTags && app.type.equals("quickGame", ignoreCase = true)) {
                            SearchTag(
                                text = stringResource(Res.string.search_quickapp_tag),
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                    if (showUpdateLog) {
                        Text(
                            text = versionSizeText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        UpdateChangeLogText(
                            text = app.changeLog.ifBlank { noChangeLogText },
                            expanded = changeLogExpanded,
                            onExpandedChange = { changeLogExpanded = it },
                            onOpenDetail = onOpenDetail,
                            onLongPress = onRowLongPress,
                        )
                    } else {
                        Text(
                            text = app.publisherName.ifBlank { app.packageName },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = versionSizeText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AppActionButton(
                    packageName = app.packageName,
                    actionText = actionText,
                    actionKind = actionKind,
                    downloadState = downloadState,
                    enabled = actionKind == AppActionKind.OPEN || !app.isDownloadBlocked(),
                    onAction = onAction,
                    onResumeDownload = onResumeDownload,
                    onInstallDownloaded = onInstallDownloaded,
                    onCancel = onCancel,
                    minHeight = 36.dp,
                    insideMargin = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                )
            }
        }
    }
    if (grouped) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpenDetail,
                    onLongClick = onRowLongPress,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            content()
        }
    } else {
        MarketCard(
            modifier = modifier.fillMaxWidth(),
            onClick = onOpenDetail,
            onLongClick = onRowLongPress,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        ) {
            content()
        }
    }
}

/** 搜索结果的来源 / 推广 / 快应用小标签。 */
@Composable
private fun SearchTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .clip(RoundedCornerShape(MarketShapeTokens.TagCorner))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun UpdateChangeLogText(
    text: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOpenDetail: () -> Unit,
    onLongPress: (() -> Unit)?,
) {
    val textMeasurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.bodySmall
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant
    val actionColor = MaterialTheme.colorScheme.primary
    val expandText = stringResource(Res.string.expand_change_log)
    val collapseText = stringResource(Res.string.collapse_change_log)

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val hasMultiLinesOfText = remember(
            text,
            widthPx,
            style,
            bodyColor,
            textMeasurer,
        ) {
            widthPx > 0 && textMeasurer.measure(
                AnnotatedString(text, spanStyle = SpanStyle(color = bodyColor)),
                style = style,
                maxLines = CollapsedChangeLogMaxLines,
                constraints = Constraints(maxWidth = widthPx),
            ).hasVisualOverflow
        }

        when {
            !hasMultiLinesOfText -> {
                ChangeLogBodyText(
                    text = text,
                    style = style,
                    color = bodyColor,
                    maxLines = Int.MAX_VALUE,
                    onOpenDetail = onOpenDetail,
                    onLongPress = onLongPress,
                )
            }

            expanded -> {
                Column(Modifier.fillMaxWidth()) {
                    ChangeLogBodyText(
                        text = text,
                        style = style,
                        color = bodyColor,
                        maxLines = Int.MAX_VALUE,
                        onOpenDetail = onOpenDetail,
                        onLongPress = onLongPress,
                    )
                    ChangeLogActionText(
                        text = collapseText,
                        style = style,
                        color = actionColor,
                        onClick = { onExpandedChange(false) },
                        onLongPress = onLongPress,
                        modifier = Modifier.align(Alignment.Start),
                    )
                }
            }

            else -> {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChangeLogBodyText(
                        text = text,
                        style = style,
                        color = bodyColor,
                        maxLines = CollapsedChangeLogMaxLines,
                        onOpenDetail = onOpenDetail,
                        onLongPress = onLongPress,
                        modifier = Modifier.weight(1f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.width(24.dp).height(22.dp).background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                ),
                            ),
                        )
                        ChangeLogActionText(
                            text = expandText,
                            style = style,
                            color = actionColor,
                            onClick = { onExpandedChange(true) },
                            onLongPress = onLongPress,
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeLogBodyText(
    text: String,
    style: TextStyle,
    color: Color,
    maxLines: Int,
    onOpenDetail: () -> Unit,
    onLongPress: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        style = style.copy(color = color),
        maxLines = maxLines,
        overflow = if (maxLines == CollapsedChangeLogMaxLines) TextOverflow.Ellipsis else TextOverflow.Clip,
        modifier = modifier.pointerInput(text, onOpenDetail, onLongPress) {
            detectTapGestures(
                onLongPress = { onLongPress?.invoke() },
                onTap = { onOpenDetail() },
            )
        },
    )
}

@Composable
private fun ChangeLogActionText(
    text: String,
    style: TextStyle,
    color: Color,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        style = style.copy(color = color, fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier.pointerInput(text, onClick, onLongPress) {
            detectTapGestures(
                onLongPress = { onLongPress?.invoke() },
                onTap = { onClick() },
            )
        },
    )
}

private fun appVersionSizeText(app: MarketAppInfo) = buildAnnotatedString {
    val fullSize = formatSize(app.apkSize)
    val deltaSize = formatSize(app.deltaSize)
    if (fullSize.isBlank()) return@buildAnnotatedString
    if (app.versionName.isNotBlank()) {
        append(app.versionName)
        append("  ")
    }
    if (deltaSize.isNotBlank() && app.deltaSize > 0L && app.deltaSize < app.apkSize) {
        append(deltaSize)
        append("  ")
        val start = length
        append(fullSize)
        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, length)
    } else {
        append(fullSize)
    }
}

@Composable
internal fun IgnoreUpdateMenu(
    show: Boolean,
    onDismiss: () -> Unit,
    onIgnoreOnce: () -> Unit,
    onIgnorePermanent: () -> Unit,
) {
    val onceText = stringResource(Res.string.ignore_once)
    val permanentText = stringResource(Res.string.ignore_permanent)
    DropdownMenu(expanded = show, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(onceText) },
            onClick = onIgnoreOnce,
        )
        DropdownMenuItem(
            text = { Text(permanentText) },
            onClick = onIgnorePermanent,
        )
    }
}
