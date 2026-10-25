package com.app.market.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.market.platform.isPredictiveBackSupported
import com.app.market.resources.Res
import com.app.market.resources.theme
import com.app.market.resources.theme_navigation_badge
import com.app.market.resources.theme_navigation_badge_summary
import com.app.market.resources.theme_page_scale
import com.app.market.resources.theme_page_scale_summary
import com.app.market.resources.theme_predictive_back
import com.app.market.resources.theme_predictive_back_summary
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.ScaleDialog
import com.app.market.ui.component.SettingsGroup
import com.app.market.ui.component.SettingsRow
import com.app.market.ui.component.SettingsSwitchRow
import com.app.market.viewmodel.ThemeSettingsUiState
import com.app.market.viewmodel.ThemeSettingsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemeSettingsScreen(
    viewModel: ThemeSettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ThemeSettingsContent(
        state = state,
        onBack = onBack,
        onEnableNavigationBadge = viewModel::setEnableNavigationBadge,
        onEnablePredictiveBack = viewModel::setEnablePredictiveBack,
        onPageScale = viewModel::setPageScale,
        predictiveBackSupported = isPredictiveBackSupported(),
        modifier = modifier,
    )
}

/**
 * 主题设置内容。
 *
 * 悬浮底栏与毛玻璃能力已随旧设计体系一并移除，因此这里只保留导航行为与界面缩放。
 */
@Composable
private fun ThemeSettingsContent(
    state: ThemeSettingsUiState,
    onBack: () -> Unit,
    onEnableNavigationBadge: (Boolean) -> Unit,
    onEnablePredictiveBack: (Boolean) -> Unit,
    onPageScale: (Float) -> Unit,
    predictiveBackSupported: Boolean,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    var showScaleDialog by rememberSaveable { mutableStateOf(false) }

    MarketScaffold(
        title = stringResource(Res.string.theme),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding, backdropModifier, scrollBehavior ->
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
            item(key = "navigation") {
                SettingsGroup {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.theme_navigation_badge),
                        summary = stringResource(Res.string.theme_navigation_badge_summary),
                        checked = state.enableNavigationBadge,
                        onCheckedChange = onEnableNavigationBadge,
                    )
                    if (predictiveBackSupported) {
                        MarketDivider(startPadding = 16.dp)
                        SettingsSwitchRow(
                            title = stringResource(Res.string.theme_predictive_back),
                            summary = stringResource(Res.string.theme_predictive_back_summary),
                            checked = state.enablePredictiveBack,
                            onCheckedChange = onEnablePredictiveBack,
                        )
                    }
                }
            }
            item(key = "page-scale") {
                SettingsGroup {
                    SettingsRow(
                        title = stringResource(Res.string.theme_page_scale),
                        summary = stringResource(Res.string.theme_page_scale_summary),
                        onClick = { showScaleDialog = !showScaleDialog },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${(state.pageScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                    MarketDivider(startPadding = 16.dp)
                    // 拖动直接调整界面缩放；松手后再落盘，避免拖动过程反复写偏好。
                    var sliderValue by remember(state.pageScale) {
                        mutableFloatStateOf(state.pageScale)
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onPageScale(sliderValue) },
                        valueRange = 0.8f..1.1f,
                        steps = 5,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
        ScaleDialog(
            show = showScaleDialog,
            onDismissRequest = { showScaleDialog = false },
            scaleProvider = { state.pageScale },
            onScaleChange = onPageScale,
        )
    }
}
