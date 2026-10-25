package com.app.market.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.installer.SavedPackage
import com.app.market.resources.Res
import com.app.market.resources.cancel
import com.app.market.resources.confirm
import com.app.market.resources.delete
import com.app.market.resources.install
import com.app.market.resources.install_success_message
import com.app.market.resources.install_success_title
import com.app.market.resources.install_waiting_message
import com.app.market.resources.installing_short
import com.app.market.resources.no_saved_packages
import com.app.market.resources.saved_package_delete_confirm
import com.app.market.resources.saved_packages
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppButtonDefaults
import com.app.market.ui.component.AppCompactButton
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.LoadingBox
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.MarketConfirmDialog
import com.app.market.ui.component.MarketDialog
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.util.formatSize
import com.app.market.ui.util.formatTimeHm
import com.app.market.viewmodel.SavedPackagesViewModel
import org.jetbrains.compose.resources.stringResource

/** 卡片之间的间距（M3E 列表节奏）。 */
private val SavedPackageCardSpacing = 12.dp

@Composable
fun SavedPackagesScreen(
    viewModel: SavedPackagesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var deletingPackage by remember { mutableStateOf<SavedPackage?>(null) }
    var retainedInstallingName by remember { mutableStateOf("") }
    var retainedInstalledName by remember { mutableStateOf("") }
    LaunchedEffect(state.installingPackage?.displayName) {
        state.installingPackage?.displayName?.let { retainedInstallingName = it }
    }
    LaunchedEffect(state.installedPackage?.displayName) {
        state.installedPackage?.displayName?.let { retainedInstalledName = it }
    }

    MarketScaffold(
        title = stringResource(Res.string.saved_packages),
        onBack = onBack,
    ) { innerPadding, backdropModifier, scrollBehavior ->
        val layoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(backdropModifier)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(SavedPackageCardSpacing),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            when {
                state.loading -> item(key = "loading") { LoadingBox(Modifier.fillParentMaxSize()) }
                state.packages.isEmpty() -> item(key = "empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(Res.string.no_saved_packages),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            itemsIndexed(
                state.packages,
                key = { _, pkg -> pkg.id },
            ) { _, pkg ->
                MarketCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(placementSpec = null),
                ) {
                    SavedPackageRow(
                        pkg = pkg,
                        onInstall = { viewModel.install(pkg) },
                        onDelete = { deletingPackage = pkg },
                    )
                }
            }
        }
    }

    DeleteSavedPackageDialog(
        show = deletingPackage != null,
        packageName = deletingPackage?.fileName.orEmpty(),
        onDismiss = { deletingPackage = null },
        onDelete = {
            deletingPackage?.let(viewModel::delete)
            deletingPackage = null
        },
    )
    InstallWaitingDialog(
        show = state.installingPackage != null,
        packageName = state.installingPackage?.displayName ?: retainedInstallingName,
    )
    InstallSuccessDialog(
        show = state.installedPackage != null,
        packageName = state.installedPackage?.displayName ?: retainedInstalledName,
        onDismiss = viewModel::dismissInstallSuccess,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallWaitingDialog(
    show: Boolean,
    packageName: String,
) {
    MarketDialog(
        show = show,
        title = stringResource(Res.string.installing_short),
        onDismissRequest = {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadingIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.install_waiting_message, packageName),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun InstallSuccessDialog(
    show: Boolean,
    packageName: String,
    onDismiss: () -> Unit,
) {
    MarketDialog(
        show = show,
        title = stringResource(Res.string.install_success_title),
        onDismissRequest = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.install_success_message, packageName),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        AppButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.confirm),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun DeleteSavedPackageDialog(
    show: Boolean,
    packageName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    MarketConfirmDialog(
        show = show,
        title = stringResource(Res.string.delete),
        message = stringResource(Res.string.saved_package_delete_confirm, packageName),
        confirmText = stringResource(Res.string.confirm),
        onConfirm = onDelete,
        onDismiss = onDismiss,
        dismissText = stringResource(Res.string.cancel),
        destructive = true,
    )
}

@Composable
private fun SavedPackageRow(
    pkg: SavedPackage,
    onInstall: () -> Unit,
    onDelete: () -> Unit,
) {
    // 卡片自身提供 16dp 内边距，这里不再重复。
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pkg.icon.isNotBlank()) {
                AppIcon(
                    url = pkg.icon,
                    contentDescription = pkg.displayName,
                    size = 48.dp,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = pkg.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = pkg.packageName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = packageSummary(pkg),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCompactButton(
                text = stringResource(Res.string.install),
                onClick = onInstall,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            AppCompactButton(
                text = stringResource(Res.string.delete),
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = AppButtonDefaults.neutral(),
            )
        }
    }
}

@Composable
private fun packageSummary(pkg: SavedPackage): String {
    val parts = listOfNotNull(
        pkg.versionName.takeIf { it.isNotBlank() },
        formatSize(pkg.size).takeIf { it.isNotBlank() },
        formatTimeHm(pkg.modifiedAt),
    )
    return parts.joinToString("  ")
}
