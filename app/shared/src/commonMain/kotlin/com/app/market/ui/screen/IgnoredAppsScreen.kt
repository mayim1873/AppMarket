package com.app.market.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.update.IgnoredUpdate
import com.app.market.resources.Res
import com.app.market.resources.ignored_apps
import com.app.market.resources.ignored_once
import com.app.market.resources.ignored_permanent
import com.app.market.resources.no_ignored
import com.app.market.resources.restore
import com.app.market.resources.system_app_tag
import com.app.market.ui.component.AppCompactButton
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SectionSpacing
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.util.appDisplayName
import com.app.market.viewmodel.IgnoredAppsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun IgnoredAppsScreen(
    viewModel: IgnoredAppsViewModel,
    onOpenDetail: (IgnoredUpdate) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permanentTitle = stringResource(Res.string.ignored_permanent)
    val onceTitle = stringResource(Res.string.ignored_once)
    val emptyHint = stringResource(Res.string.no_ignored)
    val systemTag = stringResource(Res.string.system_app_tag)

    MarketScaffold(title = stringResource(Res.string.ignored_apps), onBack = onBack) { innerPadding, _, scrollBehavior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            ignoredSection(
                title = permanentTitle,
                entries = state.permanent,
                emptyHint = emptyHint,
                systemTag = systemTag,
                isFirst = true,
                onOpenDetail = onOpenDetail,
                onRestore = { viewModel.removePermanent(it.packageName) },
            )
            ignoredSection(
                title = onceTitle,
                entries = state.once,
                emptyHint = emptyHint,
                systemTag = systemTag,
                onOpenDetail = onOpenDetail,
                onRestore = { viewModel.removeOnce(it.packageName) },
            )
        }
    }
}

private fun LazyListScope.ignoredSection(
    title: String,
    entries: List<IgnoredUpdate>,
    emptyHint: String,
    systemTag: String,
    onOpenDetail: (IgnoredUpdate) -> Unit,
    onRestore: (IgnoredUpdate) -> Unit,
    isFirst: Boolean = false,
) {
    // 区块标题自带 20dp 上边距，列表本身不再额外留白，保证分段卡片连续。
    item(key = "title-$title") {
        SectionTitle(text = title, topPadding = if (isFirst) 0.dp else SectionSpacing)
    }
    if (entries.isEmpty()) {
        item(key = "card-$title") {
            MarketCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = emptyHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        itemsIndexed(entries, key = { _, entry -> "$title:${entry.packageName}" }) { index, entry ->
            CardSegmentContainer(
                isFirst = index == 0,
                isLast = index == entries.lastIndex,
                modifier = Modifier.animateItem(placementSpec = null),
            ) {
                IgnoredAppRow(
                    entry = entry,
                    systemTag = systemTag,
                    onOpenDetail = onOpenDetail,
                    onRestore = onRestore,
                )
                if (index != entries.lastIndex) {
                    MarketDivider()
                }
            }
        }
    }
}

@Composable
private fun IgnoredAppRow(
    entry: IgnoredUpdate,
    systemTag: String,
    onOpenDetail: (IgnoredUpdate) -> Unit,
    onRestore: (IgnoredUpdate) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(entry) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            url = entry.icon,
            contentDescription = appDisplayName(entry.displayName, entry.source),
            size = 48.dp,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = appDisplayName(entry.displayName, entry.source),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (entry.isSystemApp) systemTag else entry.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AppCompactButton(
            text = stringResource(Res.string.restore),
            onClick = { onRestore(entry) },
            modifier = Modifier.width(70.dp),
        )
    }
}
