package com.app.market.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.update.UpdateHistoryEntry
import com.app.market.resources.Res
import com.app.market.resources.cancel
import com.app.market.resources.collapse_change_log
import com.app.market.resources.confirm
import com.app.market.resources.date_full
import com.app.market.resources.date_today
import com.app.market.resources.date_yesterday
import com.app.market.resources.expand_change_log
import com.app.market.resources.no_update_history
import com.app.market.resources.update_history
import com.app.market.resources.update_history_clear
import com.app.market.resources.update_history_clear_confirm
import com.app.market.resources.update_history_first_install
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.MarketConfirmDialog
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.currentLocalDate
import com.app.market.ui.util.formatTimeHm
import com.app.market.ui.util.localDateOf
import com.app.market.viewmodel.UpdateHistoryViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource

/** 同一天的历史记录分组（[entries] 保持新→旧）。 */
private data class HistoryDayGroup(val date: LocalDate, val entries: List<UpdateHistoryEntry>)

@Composable
fun UpdateHistoryScreen(
    viewModel: UpdateHistoryViewModel,
    onOpenDetail: (UpdateHistoryEntry) -> Unit,
    onBack: () -> Unit,
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    MarketScaffold(
        title = stringResource(Res.string.update_history),
        onBack = onBack,
        actions = {
            if (entries.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(Res.string.update_history_clear),
                    )
                }
            }
        },
    ) { innerPadding, backdropModifier, scrollBehavior ->
        val groups = remember(entries) {
            entries.groupBy { localDateOf(it.installedAt) }
                .map { (date, items) -> HistoryDayGroup(date, items) }
        }
        val layoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(backdropModifier)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            if (entries.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(Res.string.no_update_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            groups.forEachIndexed { groupIndex, group ->
                item(key = "day-${group.date}") {
                    SectionTitle(
                        text = dayLabel(group.date),
                        topPadding = if (groupIndex == 0) 0.dp else 20.dp,
                    )
                }
                itemsIndexed(
                    group.entries,
                    key = { _, e -> "${e.packageName}:${e.installedAt}" },
                ) { index, entry ->
                    CardSegmentContainer(
                        isFirst = index == 0,
                        isLast = index == group.entries.lastIndex,
                        modifier = Modifier.animateItem(placementSpec = null),
                    ) {
                        HistoryRow(entry = entry, onOpenDetail = onOpenDetail)
                        if (index != group.entries.lastIndex) {
                            MarketDivider(startPadding = 16.dp)
                        }
                    }
                }
            }
        }
    }

    MarketConfirmDialog(
        show = showClearDialog,
        title = stringResource(Res.string.update_history_clear),
        message = stringResource(Res.string.update_history_clear_confirm),
        confirmText = stringResource(Res.string.confirm),
        onConfirm = {
            viewModel.clear()
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false },
        dismissText = stringResource(Res.string.cancel),
        destructive = true,
    )
}

@Composable
private fun dayLabel(date: LocalDate): String {
    val today = currentLocalDate()
    return when (date) {
        today -> stringResource(Res.string.date_today)
        today.minus(1, DateTimeUnit.DAY) -> stringResource(Res.string.date_yesterday)
        else -> stringResource(Res.string.date_full, date.year, date.month.number, date.day)
    }
}

@Composable
private fun HistoryRow(
    entry: UpdateHistoryEntry,
    onOpenDetail: (UpdateHistoryEntry) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(entry) }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                url = entry.icon,
                contentDescription = appDisplayName(entry.displayName, entry.source),
                size = 48.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = appDisplayName(entry.displayName, entry.source),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (entry.previousVersionName.isEmpty()) {
                        stringResource(Res.string.update_history_first_install, entry.versionName)
                    } else {
                        "${entry.previousVersionName} → ${entry.versionName}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatTimeHm(entry.installedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (entry.changeLog.isNotBlank()) {
            var expanded by rememberSaveable(entry.packageName, entry.installedAt) { mutableStateOf(false) }
            // 三态：null 表示尚未完成首次文本测量，避免「展开」在测量前闪现
            var hasVisualOverflow by remember(entry.packageName, entry.installedAt) {
                mutableStateOf<Boolean?>(null)
            }
            val collapsedChangeLog = remember(entry.changeLog) {
                entry.changeLog.replace(WhitespaceRegex, " ").trim()
            }
            val changeLogStyle = MaterialTheme.typography.bodySmall
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = if (expanded) entry.changeLog else collapsedChangeLog,
                    style = changeLogStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (!expanded) hasVisualOverflow = result.hasVisualOverflow
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!expanded && hasVisualOverflow == true) {
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
                            Modifier.width(58.dp).fillMaxHeight().background(
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
                Text(
                    text = stringResource(Res.string.collapse_change_log),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(interactionSource = null, indication = null) {
                            expanded = false
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
