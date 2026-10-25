package com.app.market.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.domain.model.installer.InstallerMode
import com.app.market.resources.Res
import com.app.market.resources.cancel
import com.app.market.resources.installer_delete_after_install
import com.app.market.resources.installer_delete_after_install_summary
import com.app.market.resources.installer_mode_default
import com.app.market.resources.installer_mode_default_summary
import com.app.market.resources.installer_mode_root
import com.app.market.resources.installer_mode_root_summary
import com.app.market.resources.installer_mode_shizuku
import com.app.market.resources.installer_mode_shizuku_summary
import com.app.market.resources.installer_mode_third_party
import com.app.market.resources.installer_mode_third_party_summary
import com.app.market.resources.installer_no_user_action
import com.app.market.resources.installer_no_user_action_summary
import com.app.market.resources.installer_none
import com.app.market.resources.installer_pick
import com.app.market.resources.installer_save_to_downloads
import com.app.market.resources.installer_save_to_downloads_summary
import com.app.market.resources.installer_section
import com.app.market.ui.component.AppTextButton
import com.app.market.ui.component.MarketDialog
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SettingsCheckRow
import com.app.market.ui.component.SettingsGroup
import com.app.market.ui.component.SettingsSwitchRow
import com.app.market.viewmodel.InstallerSettingsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstallerSettingsScreen(
    viewModel: InstallerSettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MarketScaffold(
        title = stringResource(Res.string.installer_section),
        onBack = onBack,
    ) { innerPadding, backdropModifier, scrollBehavior ->
        val layoutDirection = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(backdropModifier)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            // 设置区块之间保持 20dp 呼吸感。
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            item(key = "mode") {
                SettingsGroup {
                    InstallerModeRow(
                        label = stringResource(Res.string.installer_mode_default),
                        summary = stringResource(Res.string.installer_mode_default_summary),
                        selected = state.mode == InstallerMode.STANDARD,
                        onClick = { viewModel.setMode(InstallerMode.STANDARD) },
                    )
                    MarketDivider(startPadding = 16.dp)
                    InstallerModeRow(
                        label = stringResource(Res.string.installer_mode_root),
                        summary = stringResource(Res.string.installer_mode_root_summary),
                        selected = state.mode == InstallerMode.ROOT,
                        onClick = { viewModel.setMode(InstallerMode.ROOT) },
                    )
                    MarketDivider(startPadding = 16.dp)
                    InstallerModeRow(
                        label = stringResource(Res.string.installer_mode_shizuku),
                        summary = stringResource(Res.string.installer_mode_shizuku_summary),
                        selected = state.mode == InstallerMode.SHIZUKU,
                        onClick = { viewModel.setMode(InstallerMode.SHIZUKU) },
                    )
                    MarketDivider(startPadding = 16.dp)
                    val selectedInstaller = state.installerCandidates.firstOrNull {
                        it.packageName == state.thirdPartyInstallerPackage
                    }
                    InstallerModeRow(
                        label = stringResource(Res.string.installer_mode_third_party),
                        summary = selectedInstaller?.label
                            ?: stringResource(Res.string.installer_mode_third_party_summary),
                        selected = state.mode == InstallerMode.THIRD_PARTY,
                        onClick = viewModel::showThirdPartyInstallerPicker,
                    )
                }
            }

            item(key = "save") {
                val deleteAfterInstall = state.mode == InstallerMode.THIRD_PARTY
                SettingsGroup {
                    SettingsSwitchRow(
                        title = stringResource(
                            if (deleteAfterInstall) {
                                Res.string.installer_delete_after_install
                            } else {
                                Res.string.installer_save_to_downloads
                            }
                        ),
                        summary = stringResource(
                            if (deleteAfterInstall) {
                                Res.string.installer_delete_after_install_summary
                            } else {
                                Res.string.installer_save_to_downloads_summary
                            }
                        ),
                        checked = if (deleteAfterInstall) !state.saveToDownloads else state.saveToDownloads,
                        onCheckedChange = { checked ->
                            viewModel.setSaveToDownloads(if (deleteAfterInstall) !checked else checked)
                        },
                    )
                    if (
                        state.mode == InstallerMode.STANDARD &&
                        state.userActionNotRequiredConfigurable
                    ) {
                        MarketDivider(startPadding = 16.dp)
                        SettingsSwitchRow(
                            title = stringResource(Res.string.installer_no_user_action),
                            summary = stringResource(Res.string.installer_no_user_action_summary),
                            checked = state.userActionNotRequiredEnabled,
                            onCheckedChange = viewModel::setUserActionNotRequiredEnabled,
                        )
                    }
                }
            }
        }
    }

    MarketDialog(
        show = state.showInstallerPicker,
        title = stringResource(Res.string.installer_pick),
        onDismissRequest = viewModel::dismissThirdPartyInstallerPicker,
    ) {
        // 弹窗内容自身可滚动（MarketDialog 已做限高），这里用普通 Column 承载候选列表。
        if (state.installerCandidates.isEmpty()) {
            Text(
                text = stringResource(Res.string.installer_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.installerCandidates.forEach { candidate ->
                    key(candidate.packageName) {
                        SettingsCheckRow(
                            title = candidate.label,
                            summary = candidate.packageName,
                            selected = candidate.packageName == state.thirdPartyInstallerPackage,
                            onClick = { viewModel.selectThirdPartyInstaller(candidate) },
                        )
                    }
                }
            }
        }
        AppTextButton(
            text = stringResource(Res.string.cancel),
            onClick = viewModel::dismissThirdPartyInstallerPicker,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InstallerModeRow(
    label: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SettingsCheckRow(
        title = label,
        summary = summary,
        selected = selected,
        onClick = onClick,
    )
}
