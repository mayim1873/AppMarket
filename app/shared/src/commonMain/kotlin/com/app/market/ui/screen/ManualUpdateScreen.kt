package com.app.market.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.download.DownloadState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.resources.Res
import com.app.market.resources.manual_update
import com.app.market.resources.manual_update_available
import com.app.market.resources.manual_update_check
import com.app.market.resources.manual_update_current
import com.app.market.resources.manual_update_no_apk
import com.app.market.resources.manual_update_package
import com.app.market.resources.manual_update_recognized_no_update
import com.app.market.resources.manual_update_version_code
import com.app.market.resources.update
import com.app.market.ui.component.AppActionButton
import com.app.market.ui.component.AppButton
import com.app.market.ui.component.AppButtonText
import com.app.market.ui.component.AppIcon
import com.app.market.ui.component.MarketCard
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageHorizontalPadding
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.model.AppActionKind
import com.app.market.ui.util.appDisplayName
import com.app.market.ui.util.formatSize
import com.app.market.viewmodel.ManualUpdateViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManualUpdateScreen(
    viewModel: ManualUpdateViewModel,
    onOpenDetail: (MarketAppInfo) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates = viewModel.downloadStates.collectAsStateWithLifecycle()
    val updateText = stringResource(Res.string.update)

    MarketScaffold(
        title = stringResource(Res.string.manual_update),
        onBack = onBack
    ) { innerPadding, _, scrollBehavior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = PageHorizontalPadding,
                end = PageHorizontalPadding,
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            item(key = "package") {
                OutlinedTextField(
                    value = state.packageName,
                    onValueChange = viewModel::setPackageName,
                    label = { Text(stringResource(Res.string.manual_update_package)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "version") {
                OutlinedTextField(
                    value = state.versionCode,
                    onValueChange = viewModel::setVersionCode,
                    label = { Text(stringResource(Res.string.manual_update_version_code)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item(key = "check") {
                AppButton(
                    onClick = viewModel::check,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                    minHeight = 48.dp,
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        AppButtonText(stringResource(Res.string.manual_update_check))
                    }
                }
            }

            if (state.errorMessage.isNotBlank()) {
                item(key = "error") {
                    MarketCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }

            if (state.noApk) {
                item(key = "no_apk") {
                    MarketCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(Res.string.manual_update_no_apk),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (state.recognizedNoUpdate) {
                                    Text(
                                        text = stringResource(Res.string.manual_update_recognized_no_update),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.result?.let { app ->
                item(key = "result") {
                    val packageName = app.packageName
                    val downloadState by remember(packageName) {
                        derivedStateOf { downloadStates.value[packageName] }
                    }
                    ManualUpdateResultCard(
                        app = app,
                        currentVersionCode = state.versionCode,
                        actionText = updateText,
                        downloadState = downloadState,
                        onOpenDetail = { onOpenDetail(app) },
                        onDownload = { viewModel.download(app) },
                        onInstallDownloaded = viewModel::installDownloaded,
                        onCancel = viewModel::cancelDownload,
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualUpdateResultCard(
    app: MarketAppInfo,
    currentVersionCode: String,
    actionText: String,
    downloadState: DownloadState?,
    onOpenDetail: () -> Unit,
    onDownload: () -> Unit,
    onInstallDownloaded: (String) -> Unit,
    onCancel: (String) -> Unit,
) {
    MarketCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenDetail,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppIcon(
                url = app.icon,
                contentDescription = appDisplayName(app.displayName, app.source),
                size = 58.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = appDisplayName(app.displayName, app.source),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        Res.string.manual_update_current,
                        "-",
                        currentVersionCode,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        Res.string.manual_update_available,
                        app.versionName,
                        app.versionCode.toString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatSize(app.apkSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            AppActionButton(
                packageName = app.packageName,
                actionText = actionText,
                actionKind = AppActionKind.UPDATE,
                downloadState = downloadState,
                onAction = onDownload,
                onResumeDownload = onDownload,
                onInstallDownloaded = onInstallDownloaded,
                onCancel = onCancel,
                modifier = Modifier.width(70.dp),
            )
        }
    }
}
