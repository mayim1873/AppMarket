package com.app.market.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.resources.Res
import com.app.market.resources.cancel
import com.app.market.resources.clear_history
import com.app.market.resources.nav_search
import com.app.market.resources.no_results
import com.app.market.resources.open
import com.app.market.resources.reserve
import com.app.market.resources.search_hint
import com.app.market.resources.search_history
import com.app.market.resources.update
import com.app.market.ui.component.AppRow
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.theme.MarketShapeTokens
import com.app.market.ui.util.installActionText
import com.app.market.viewmodel.SearchUiState
import com.app.market.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun SearchTab(
    viewModel: SearchViewModel,
    bottomPadding: Dp,
    onOpenDetail: (MarketAppInfo) -> Unit,
    isCurrentPage: Boolean = true,
    focusRequestId: Int = 0,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates = viewModel.downloadStates.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 无限滚动：进入底部区域时触发；distinctUntilChanged 避免新搜索后旧的「已到底」状态重复触发。
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= 0 && info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { atBottom -> if (atBottom) viewModel.loadMore() }
    }
    // 每次搜索完成回到顶部（searchEpoch 变化，同关键词重搜也会触发）。
    LaunchedEffect(state.searchEpoch) {
        if (state.searchEpoch > 0) listState.scrollToItem(0)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var handledFocusRequestId by remember { mutableIntStateOf(focusRequestId) }

    LaunchedEffect(focusRequestId) {
        if (isCurrentPage && focusRequestId != handledFocusRequestId) {
            handledFocusRequestId = focusRequestId
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 搜索「激活」判定：输入框聚焦或已有关键词（例如点历史词触发，输入框并未聚焦）。
    val searchActive = isFocused || state.keyword.isNotEmpty()
    val dismissSearchInput: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    val cancelSearch: () -> Unit = {
        viewModel.clearSearch()
        dismissSearchInput()
    }

    // 搜索激活时，返回键先清空搜索（优先于 pager 的返回首页）。
    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = isCurrentPage && searchActive,
        onBackCompleted = cancelSearch,
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = PageVerticalPadding,
        bottom = bottomPadding + PageVerticalPadding,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.nav_search),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            SearchField(
                keyword = state.keyword,
                onKeywordChange = viewModel::setKeyword,
                onSearch = {
                    viewModel.runSearch()
                    dismissSearchInput()
                },
                searchActive = searchActive,
                onCancel = cancelSearch,
                focusRequester = focusRequester,
                interactionSource = interactionSource,
            )
            Box(Modifier.fillMaxHeight()) {
                Crossfade(
                    targetState = state.loading && state.results.isEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    label = "search",
                ) { fullScreenLoading ->
                    if (fullScreenLoading) {
                        LoadingBox(Modifier.fillMaxSize().padding(contentPadding))
                    } else {
                        SearchResultsList(
                            state = state,
                            listState = listState,
                            downloadStates = downloadStates,
                            contentPadding = contentPadding,
                            scrollBehavior = scrollBehavior,
                            viewModel = viewModel,
                            onSearchHistory = { item ->
                                viewModel.searchWith(item)
                                dismissSearchInput()
                            },
                            onOpenDetail = onOpenDetail,
                        )
                    }
                }
            }
        }
    }
}

/** M3E 搜索框：28dp 圆角、搜索图标 + 清除按钮，取消按钮随搜索激活状态展开。 */
@Composable
private fun SearchField(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchActive: Boolean,
    onCancel: () -> Unit,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = {
                Text(
                    text = stringResource(Res.string.search_hint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    IconButton(onClick = { onKeywordChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            interactionSource = interactionSource,
        )
        AnimatedVisibility(
            visible = searchActive,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut(),
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(Res.string.cancel),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun SearchResultsList(
    state: SearchUiState,
    listState: LazyListState,
    downloadStates: State<Map<String, DownloadState>>,
    contentPadding: PaddingValues,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    viewModel: SearchViewModel,
    onSearchHistory: (String) -> Unit,
    onOpenDetail: (MarketAppInfo) -> Unit,
) {
    val installText = installActionText()
    val updateText = stringResource(Res.string.update)
    val openText = stringResource(Res.string.open)
    val reserveText = stringResource(Res.string.reserve)
    val noResults = stringResource(Res.string.no_results)
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding,
    ) {
        // 结果已存在时的紧凑 spinner；首次搜索用全屏居中版本。
        if (state.loading && state.results.isNotEmpty()) item(key = "loading") { LoadingBox() }
        if (state.errorMessage.isNotEmpty()) {
            item(key = "error") {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else if (state.showNoResults) {
            item(key = "empty") {
                Text(
                    text = noResults,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (state.results.isEmpty() && state.keyword.isBlank() && state.history.isNotEmpty()) {
            item(key = "history") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.search_history),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        TextButton(onClick = viewModel::clearHistory) {
                            Text(text = stringResource(Res.string.clear_history))
                        }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.history.forEach { item ->
                            val selected = state.selectedHistory == item
                            HistoryChip(
                                text = item,
                                selected = selected,
                                onClick = { onSearchHistory(item) },
                                onLongClick = { viewModel.selectHistory(item) },
                                onRemove = { viewModel.removeHistory(item) },
                            )
                        }
                    }
                }
            }
        }

        items(state.results, key = { it.app.packageName }) { item ->
            val app = item.app
            val packageName = app.packageName
            val downloadState by remember(packageName) {
                derivedStateOf { downloadStates.value[packageName] }
            }
            AppRow(
                app = app,
                modifier = Modifier.animateItem(),
                actionText = when (item.actionKind) {
                    AppActionKind.INSTALL -> installText
                    AppActionKind.UPDATE -> updateText
                    AppActionKind.OPEN -> openText
                    AppActionKind.RESERVE -> reserveText
                },
                actionKind = item.actionKind,
                downloadState = downloadState,
                onOpenDetail = { onOpenDetail(app) },
                onAction = { viewModel.onAction(item) },
                onResumeDownload = { viewModel.download(app, item.actionKind == AppActionKind.UPDATE) },
                onInstallDownloaded = viewModel::installDownloaded,
                onCancel = viewModel::cancelDownload,
                showSearchTags = true,
                showSourceTag = state.sources.size > 1,
            )
        }

        if (state.loadingMore) {
            item(key = "loadmore") { LoadingBox() }
        }
    }
}

/** 搜索历史胶囊：长按选中后可删除。 */
@Composable
private fun HistoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(MarketShapeTokens.TagCorner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = onRemove)
                        .padding(start = 8.dp)
                        .size(14.dp),
                )
            }
        }
    }
}
