package com.app.market.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.platform.UiPlatform
import com.app.market.resources.Res
import com.app.market.resources.app_list_permission_refresh_hint
import com.app.market.resources.app_list_permission_required
import com.app.market.resources.collapse_change_log
import com.app.market.resources.expand_change_log
import com.app.market.resources.go_to_settings
import com.app.market.resources.ignore_once
import com.app.market.resources.ignore_permanent
import com.app.market.resources.nav_updates
import com.app.market.resources.no_change_log
import com.app.market.resources.no_updates
import com.app.market.resources.num_updates_pending
import com.app.market.resources.update
import com.app.market.resources.update_all
import com.app.market.resources.update_check_failed
import com.app.market.resources.version_transition
import com.app.market.resources.version_transition_label
import com.app.market.ui.component.AppActionButton
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.MainTabScaffold
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.theme.MarketShapeTokens
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.formatSize
import com.app.market.viewmodel.UpdatesViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun UpdatesTab(
    viewModel: UpdatesViewModel,
    onOpenDetail: (MarketAppInfo) -> Unit,
) {
    val uiPlatform = koinInject<UiPlatform>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates = viewModel.downloadStates.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    MainTabScaffold(
        title = stringResource(Res.string.nav_updates)
    ) { topPadding, _, scrollBehavior ->
        val listPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topPadding + PageVerticalPadding,
            bottom = PageVerticalPadding,
        )
        val content = when {
            state.updates.isNotEmpty() -> UpdatesContent.Cards
            state.loading -> UpdatesContent.Loading
            state.appListPermissionRequired -> UpdatesContent.PermissionRequired
            state.checkFailed -> UpdatesContent.Error
            else -> UpdatesContent.Empty
        }
        // 全屏状态与卡片列表之间淡入淡出；按状态形态作 key，数据刷新不会重放整棵树。
        Crossfade(
            targetState = content,
            animationSpec = tween(220),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            label = "updates",
        ) { target ->
            when (target) {
                UpdatesContent.Loading -> LoadingBox(Modifier.fillMaxSize().padding(listPadding))
                UpdatesContent.PermissionRequired -> AppListPermissionRequiredState(
                    modifier = Modifier.fillMaxSize().padding(listPadding),
                    uiPlatform = uiPlatform,
                )

                UpdatesContent.Error -> UpdatesHint(
                    text = stringResource(Res.string.update_check_failed),
                    icon = Icons.Filled.SearchOff,
                    padding = listPadding,
                )

                UpdatesContent.Empty -> UpdatesHint(
                    text = stringResource(Res.string.no_updates),
                    icon = Icons.Filled.CheckCircle,
                    padding = listPadding,
                )

                UpdatesContent.Cards -> UpdatesCardList(
                    updates = state.updates,
                    downloadStates = downloadStates,
                    listState = listState,
                    listPadding = listPadding,
                    viewModel = viewModel,
                    onOpenDetail = onOpenDetail,
                    modifier = Modifier
                        .fillMaxHeight()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                )
            }
        }
    }
}

private enum class UpdatesContent { Loading, PermissionRequired, Error, Empty, Cards }

@Composable
private fun UpdatesCardList(
    updates: List<MarketAppInfo>,
    downloadStates: State<Map<String, DownloadState>>,
    listState: LazyListState,
    listPadding: PaddingValues,
    viewModel: UpdatesViewModel,
    onOpenDetail: (MarketAppInfo) -> Unit,
    modifier: Modifier,
) {
    val updateActionText = stringResource(Res.string.update)
    val totalUpdateSize = remember(updates) {
        updates.sumOf { app -> app.deltaSize.takeIf { it > 0 } ?: app.apkSize }
    }
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = listPadding,
    ) {
        item(key = "summary") {
            UpdatesSummaryCard(
                count = updates.size,
                totalSize = totalUpdateSize,
                onUpdateAll = { viewModel.downloadAll(updates) },
            )
        }
        itemsIndexed(updates, key = { _, app -> "${app.source}:${app.packageName}" }) { _, app ->
            val packageName = app.packageName
            val downloadState by remember(packageName) {
                derivedStateOf { downloadStates.value[packageName] }
            }
            UpdateItemCard(
                app = app,
                downloadState = downloadState,
                actionText = updateActionText,
                onOpenDetail = { onOpenDetail(app) },
                onAction = { viewModel.download(app) },
                onInstallDownloaded = viewModel::installDownloaded,
                onCancel = viewModel::cancelDownload,
                onIgnoreOnce = { viewModel.ignoreOnce(app) },
                onIgnorePermanent = { viewModel.ignorePermanently(app) },
            )
        }
    }
}

/** 顶部汇总卡：M3E 的 primaryContainer 大色块 + 填充按钮。 */
@Composable
private fun UpdatesSummaryCard(
    count: Int,
    totalSize: Long,
    onUpdateAll: () -> Unit,
) {
    MarketCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = PaddingValues(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.num_updates_pending, count),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = formatSize(totalSize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }
            AppButton(
                text = stringResource(Res.string.update_all),
                onClick = onUpdateAll,
                minHeight = 40.dp,
                insideMargin = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

// 也被更新历史页复用：折叠态把日志空白压平成单行。
internal val WhitespaceRegex = Regex("\\s+")

@Composable
private fun UpdateItemCard(
    app: MarketAppInfo,
    downloadState: DownloadState?,
    actionText: String,
    onOpenDetail: () -> Unit,
    onAction: () -> Unit,
    onInstallDownloaded: (String) -> Unit,
    onCancel: (String) -> Unit,
    onIgnoreOnce: () -> Unit,
    onIgnorePermanent: () -> Unit,
) {
    var expanded by remember(app.packageName, app.versionCode) { mutableStateOf(false) }
    val changeLog = app.changeLog.ifBlank { stringResource(Res.string.no_change_log) }
    // 折叠态把换行等空白压平成单行连续文字，避免首行只是「新增」之类的小标题。
    val collapsedChangeLog = remember(changeLog) {
        changeLog.replace(WhitespaceRegex, " ").trim()
    }
    // 「展开」除了展开日志还负责露出忽略操作，因此折叠态始终显示，不做溢出检测。
    val showMore = !expanded
    val changeLogStyle = MaterialTheme.typography.bodySmall

    MarketCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenDetail,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(
                url = app.icon,
                contentDescription = appDisplayName(app.displayName, app.source),
                size = 52.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appDisplayName(app.displayName, app.source),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.version_transition_label),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            Res.string.version_transition,
                            app.installedVersionName.ifBlank { "-" },
                            app.versionName,
                        ),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = updateSizeText(app),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppActionButton(
                packageName = app.packageName,
                actionText = actionText,
                actionKind = AppActionKind.UPDATE,
                downloadState = downloadState,
                onAction = onAction,
                onResumeDownload = onAction,
                onInstallDownloaded = onInstallDownloaded,
                onCancel = onCancel,
                minHeight = 36.dp,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth()) {
            SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (expanded) changeLog else collapsedChangeLog,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Clip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = changeLogStyle,
                )
            }
            if (showMore) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(IntrinsicSize.Min)
                        .clickable(interactionSource = null, indication = null) {
                            expanded = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.width(48.dp).fillMaxHeight().background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                1f to MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.expand_change_log),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = changeLogStyle,
                    )
                }
            }
        }
        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionLink(
                        text = stringResource(Res.string.ignore_once),
                        onClick = onIgnoreOnce,
                    )
                    ActionLink(
                        text = stringResource(Res.string.ignore_permanent),
                        onClick = onIgnorePermanent,
                    )
                }
                ActionLink(
                    text = stringResource(Res.string.collapse_change_log),
                    onClick = { expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ActionLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

internal fun updateSizeText(app: MarketAppInfo) = buildAnnotatedString {
    val fullSize = formatSize(app.apkSize)
    val hasDelta = app.deltaSize > 0L && app.deltaSize < app.apkSize
    if (!hasDelta) {
        append(fullSize)
        return@buildAnnotatedString
    }

    append(formatSize(app.deltaSize))
    append("  ")
    val fullSizeStart = length
    append(fullSize)
    addStyle(
        SpanStyle(textDecoration = TextDecoration.LineThrough),
        fullSizeStart,
        length,
    )
}

@Composable
private fun UpdatesHint(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppListPermissionRequiredState(
    modifier: Modifier = Modifier,
    uiPlatform: UiPlatform,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(Res.string.app_list_permission_required),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.app_list_permission_refresh_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        AppButton(
            text = stringResource(Res.string.go_to_settings),
            onClick = { uiPlatform.openAppSettings() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
