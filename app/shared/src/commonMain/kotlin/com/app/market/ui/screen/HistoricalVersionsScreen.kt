package com.app.market.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.download.DownloadPhase
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.download.DownloadTaskKey
import com.app.market.domain.model.market.HistoricalVersion
import com.app.market.resources.Res
import com.app.market.resources.date_full
import com.app.market.resources.download
import com.app.market.resources.download_finished
import com.app.market.resources.download_paused
import com.app.market.resources.downloading_short
import com.app.market.resources.historical_versions_title
import com.app.market.resources.installing_short
import com.app.market.resources.no_historical_versions
import com.app.market.resources.retry
import com.app.market.resources.waiting_install_confirmation
import com.app.market.ui.component.AppCompactButton
import com.app.market.ui.component.AppTextButton
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageHorizontalPadding
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.util.formatSize
import com.app.market.ui.util.localDateOf
import com.app.market.viewmodel.HistoricalVersionsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoricalVersionsScreen(
    viewModel: HistoricalVersionsViewModel,
    appId: Long,
    packageName: String,
    displayName: String,
    onBack: () -> Unit,
) {
    LaunchedEffect(appId, packageName) { viewModel.load(appId, packageName) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, appId, packageName) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= 0 && layout.totalItemsCount > 0 && lastVisible >= layout.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { atBottom ->
                if (atBottom) viewModel.loadMore(appId, packageName)
            }
    }

    MarketScaffold(
        title = stringResource(Res.string.historical_versions_title, displayName),
        onBack = onBack,
    ) { innerPadding, _, scrollBehavior ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = PageHorizontalPadding,
                end = PageHorizontalPadding,
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            if (state.loading) {
                item(key = "loading") { LoadingBox(Modifier.fillParentMaxSize()) }
            } else if (state.items.isEmpty() && state.errorMessage.isBlank()) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(Res.string.no_historical_versions),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (state.errorMessage.isNotBlank()) {
                item(key = "error-${state.errorMessage}") {
                    MarketCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (state.items.isEmpty()) 0.dp else PageVerticalPadding),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = state.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                AppTextButton(
                                    text = stringResource(Res.string.retry),
                                    onClick = { viewModel.retry(appId, packageName) },
                                    modifier = Modifier.align(Alignment.Start),
                                )
                            }
                        }
                    }
                }
            }

            itemsIndexed(
                items = state.items,
                key = { _, version -> version.versionId },
            ) { index, version ->
                val downloadState = downloadStates[
                    DownloadTaskKey(version.packageName, version.versionCode)
                ]
                CardSegmentContainer(
                    isFirst = index == 0,
                    isLast = index == state.items.lastIndex,
                    horizontalPadding = 0.dp,
                ) {
                    HistoricalVersionRow(
                        version = version,
                        downloadState = downloadState,
                        onDownload = { viewModel.download(version) },
                        onPause = { viewModel.pause(version) },
                    )
                    if (index != state.items.lastIndex) {
                        MarketDivider()
                    }
                }
            }

            if (state.loadingMore) {
                item(key = "loading-more") { LoadingBox(Modifier.fillMaxWidth().padding(20.dp)) }
            }
        }
    }
}

@Composable
private fun HistoricalVersionRow(
    version: HistoricalVersion,
    downloadState: DownloadState?,
    onDownload: () -> Unit,
    onPause: () -> Unit,
) {
    val timestamp = version.updatedAt.takeIf { it > 0L } ?: version.createdAt
    val dateText = if (timestamp > 0L) {
        val date = localDateOf(timestamp)
        stringResource(Res.string.date_full, date.year, date.month.number, date.day)
    } else {
        ""
    }
    val summary = listOf(formatSize(version.size), dateText).filter(String::isNotBlank).joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = version.versionName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
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
        AppCompactButton(
            text = when {
                downloadState?.isComplete == true -> stringResource(Res.string.download_finished)
                downloadState?.phase == DownloadPhase.AWAITING_USER_ACTION -> {
                    stringResource(Res.string.waiting_install_confirmation)
                }

                downloadState?.phase == DownloadPhase.INSTALLING -> stringResource(Res.string.installing_short)
                downloadState?.phase in ActiveHistoricalTransferPhases -> stringResource(Res.string.downloading_short)
                downloadState?.phase == DownloadPhase.PAUSED -> stringResource(Res.string.download_paused)
                else -> stringResource(Res.string.download)
            },
            onClick = {
                if (downloadState?.phase in ActiveHistoricalTransferPhases) onPause()
                else onDownload()
            },
            enabled = historicalDownloadButtonEnabled(downloadState),
        )
    }
}

private val ActiveHistoricalTransferPhases = setOf(
    DownloadPhase.QUEUED,
    DownloadPhase.DOWNLOADING,
)

private val BlockingHistoricalDownloadPhases = setOf(
    DownloadPhase.INSTALLING,
    DownloadPhase.AWAITING_USER_ACTION,
)

internal fun historicalDownloadButtonEnabled(
    state: DownloadState?,
): Boolean = state?.isComplete != true && state?.phase !in BlockingHistoricalDownloadPhases
