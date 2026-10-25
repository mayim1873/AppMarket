package com.app.market.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.app.market.domain.model.market.MarketAppInfo
import com.app.market.domain.model.preference.HomePage
import com.app.market.platform.UiPlatform
import com.app.market.resources.Res
import com.app.market.resources.nav_search
import com.app.market.resources.nav_settings
import com.app.market.resources.nav_today
import com.app.market.resources.nav_updates
import com.app.market.ui.component.MarketNavItem
import com.app.market.ui.component.MarketNavigationBar
import com.app.market.ui.component.MarketNavigationRail
import com.app.market.ui.screen.SearchTab
import com.app.market.ui.screen.SettingsTab
import com.app.market.ui.screen.TodayTab
import com.app.market.ui.screen.UpdatesTab
import com.app.market.ui.util.rememberIsWideScreen
import com.app.market.viewmodel.InstallerSettingsViewModel
import com.app.market.viewmodel.SearchViewModel
import com.app.market.viewmodel.TodayViewModel
import com.app.market.viewmodel.UpdatesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.abs

// 主页签集合按平台能力裁剪：桌面端（无法扫描已装应用 / 安装）不含「更新」。
private enum class MainTab { Today, Updates, Search, Settings }

@Composable
fun MainPage(
    navigator: Navigator,
    updatesViewModel: UpdatesViewModel,
    searchViewModel: SearchViewModel,
    installerSettingsViewModel: InstallerSettingsViewModel,
    todayViewModel: TodayViewModel,
    pendingSearchKeyword: String? = null,
    onPendingSearchConsumed: () -> Unit = {},
) {
    val uiPlatform = koinInject<UiPlatform>()
    val updatesState by updatesViewModel.uiState.collectAsStateWithLifecycle()
    val appManagementSupported = uiPlatform.packageInstallationSupported
    val tabs = remember(appManagementSupported) {
        buildList {
            add(MainTab.Today)
            if (appManagementSupported) add(MainTab.Updates)
            add(MainTab.Search)
            add(MainTab.Settings)
        }
    }
    val searchPage = tabs.indexOf(MainTab.Search)
    fun homeIndexOf(page: HomePage): Int =
        tabs.indexOf(page.toTab()).takeIf { it >= 0 } ?: 0

    // 初始页读取一次种子值（不跳转）；后续随返回目标响应式变化。
    val homePage by updatesViewModel.homePage.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = homeIndexOf(updatesViewModel.homePage.value),
        pageCount = { tabs.size },
    )
    val mainPagerState = rememberMainPagerState(pagerState)
    val selectedPage = mainPagerState.selectedPage
    val isWideScreen = rememberIsWideScreen()
    var notificationPermissionRequested by remember { mutableStateOf(false) }
    var searchFocusRequestId by remember { mutableIntStateOf(0) }
    val onSearchTabClick = {
        if (selectedPage == searchPage && !mainPagerState.isNavigating) {
            searchFocusRequestId += 1
        } else {
            mainPagerState.animateToPage(searchPage)
        }
    }
    val onTabClick: (Int, MainTab) -> Unit = { index, tab ->
        if (tab == MainTab.Search) onSearchTabClick() else mainPagerState.animateToPage(index)
    }
    val startInitialCheckAfterAppListPermission = {
        val launched = uiPlatform.requestInstalledAppsPermission {
            updatesViewModel.startInitialCheck()
        }
        if (!launched) updatesViewModel.startInitialCheck()
    }

    LaunchedEffect(pendingSearchKeyword) {
        val keyword = pendingSearchKeyword ?: return@LaunchedEffect
        mainPagerState.animateToPage(searchPage)
        if (keyword.isNotEmpty()) searchViewModel.searchWith(keyword) else searchFocusRequestId += 1
        onPendingSearchConsumed()
    }

    // 用户滑动 pager 时也同步选中页（而不只是点击 tab 的 animateToPage）。
    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    // 更新检查（含权限流程）只在支持应用管理的平台执行。
    LaunchedEffect(Unit) {
        if (appManagementSupported && !notificationPermissionRequested) {
            notificationPermissionRequested = true
            val launched = uiPlatform.requestPostNotificationsPermission {
                startInitialCheckAfterAppListPermission()
            }
            if (!launched) startInitialCheckAfterAppListPermission()
        }
    }

    MainScreenBackHandler(mainPagerState, navigator, homeIndexOf(homePage))

    val openDetail: (MarketAppInfo) -> Unit = { app ->
        // 搜索页可能给出 vivo 条目，详情要打对应源的接口。
        navigator.push(Route.AppDetail(app.appId, app.packageName, app.displayName, source = app.source))
    }

    val pagerContent: @Composable (Modifier) -> Unit = { pagerModifier ->
        HorizontalPager(
            modifier = pagerModifier,
            state = mainPagerState.pagerState,
            overscrollEffect = null,
            verticalAlignment = Alignment.Top,
            beyondViewportPageCount = 1,
        ) { page ->
            when (tabs[page]) {
                MainTab.Today -> TodayTab(
                    viewModel = todayViewModel,
                    updatesViewModel = updatesViewModel.takeIf { appManagementSupported },
                    onClickViewUpdates = { tabs.indexOf(MainTab.Updates).takeIf { it >= 0 }?.let(mainPagerState::animateToPage) },
                    onClickArticle = { article ->
                        navigator.push(Route.TodayArticle(article.rId))
                    },
                )

                MainTab.Updates -> UpdatesTab(
                    viewModel = updatesViewModel,
                    onOpenDetail = openDetail,
                )

                MainTab.Search -> SearchTab(
                    viewModel = searchViewModel,
                    onOpenDetail = openDetail,
                    isCurrentPage = selectedPage == searchPage,
                    focusRequestId = searchFocusRequestId,
                )

                MainTab.Settings -> SettingsTab(
                    updatesViewModel = updatesViewModel,
                    installerSettingsViewModel = installerSettingsViewModel,
                    appManagementSupported = appManagementSupported,
                    onNavigateDeviceProfile = { navigator.push(Route.DeviceProfile) },
                    onNavigateInstaller = { navigator.push(Route.InstallerSettings) },
                    onNavigateIgnored = { navigator.push(Route.IgnoredApps) },
                    onNavigateManualUpdate = { navigator.push(Route.ManualUpdate) },
                    onNavigateUpdateHistory = { navigator.push(Route.UpdateHistory) },
                    onNavigateSavedPackages = { navigator.push(Route.SavedPackages) },
                    onNavigateDownloadingApps = { navigator.push(Route.DownloadingApps) },
                    onNavigateAbout = { navigator.push(Route.About) },
                    onNavigateTheme = { navigator.push(Route.ThemeSettings) },
                )
            }
        }
    }

    val navItems = tabs.map { tab ->
        MarketNavItem(
            icon = tab.icon,
            label = stringResource(tab.labelRes),
            badgeCount = navigationBadgeCount(tab, updatesState.updates.size),
        )
    }

    if (isWideScreen) {
        Row(Modifier.fillMaxSize()) {
            MarketNavigationRail(
                items = navItems,
                selectedIndex = selectedPage,
                onSelected = { index -> onTabClick(index, tabs[index]) },
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.displayCutout.union(WindowInsets.navigationBars)),
            )
            pagerContent(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    // 侧栏已消费起始侧的刘海 / 导航栏内边距，末端侧在此补上。
                    .consumeWindowInsets(
                        WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                            .only(WindowInsetsSides.Start),
                    )
                    .windowInsetsPadding(
                        WindowInsets.systemBars.union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.End),
                    ),
            )
        }
    } else {
        // 固定底栏由 Scaffold 的 bottomBar 持有，内容区自动避让（含系统导航栏内边距）。
        Scaffold(
            bottomBar = {
                MarketNavigationBar(
                    items = navItems,
                    selectedIndex = selectedPage,
                    onSelected = { index -> onTabClick(index, tabs[index]) },
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            pagerContent(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
            )
        }
    }
}

private fun HomePage.toTab(): MainTab = when (this) {
    HomePage.TODAY -> MainTab.Today
    HomePage.UPDATES -> MainTab.Updates
    HomePage.SEARCH -> MainTab.Search
}

private val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.Today -> Icons.Filled.Explore
        MainTab.Updates -> Icons.Filled.Update
        MainTab.Search -> Icons.Filled.Search
        MainTab.Settings -> Icons.Filled.Settings
    }

private val MainTab.labelRes
    get() = when (this) {
        MainTab.Today -> Res.string.nav_today
        MainTab.Updates -> Res.string.nav_updates
        MainTab.Search -> Res.string.nav_search
        MainTab.Settings -> Res.string.nav_settings
    }

private fun navigationBadgeCount(tab: MainTab, updateCount: Int): Int =
    if (tab == MainTab.Updates && updateCount > 0) updateCount else 0

@Stable
class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return
        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true
        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.springAnimateToPage(targetIndex)
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) selectedPage = pagerState.currentPage
                }
            }
        }
    }

    /** 手动滑动后同步 [selectedPage]（tab 点击动画期间不干预）。 */
    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

private suspend fun PagerState.springAnimateToPage(target: Int) {
    if (target !in 0 until pageCount) return
    var shouldSnapToTarget = false
    scroll(MutatePriority.UserInput) {
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val distance = target - currentPage - currentPageOffsetFraction
        val scrollPixels = distance * pageSize
        if (abs(scrollPixels) <= 0.5f) return@scroll

        var consumedScroll = 0f
        var skipScroll = false
        Animatable(0f).animateTo(
            targetValue = scrollPixels,
            animationSpec = PagerNavigationSpringSpec,
        ) {
            if (skipScroll) return@animateTo

            val delta = value - consumedScroll
            if (abs(delta) > 0.5f) {
                val consumed = scrollBy(delta)
                consumedScroll += consumed
                if (abs(delta - consumed) > 0.1f) {
                    shouldSnapToTarget = true
                    skipScroll = true
                }
            } else {
                consumedScroll = value
            }

            if (abs(velocity) < 0.1f && abs(scrollPixels - consumedScroll) < 1.0f) {
                skipScroll = true
            }
        }

        val remaining = scrollPixels - consumedScroll
        if (abs(remaining) > 0.5f) {
            scrollBy(remaining)
        }
    }

    if (shouldSnapToTarget || currentPage != target) {
        scrollToPage(target)
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState =
    remember(pagerState, coroutineScope) { MainPagerState(pagerState, coroutineScope) }

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navigator: Navigator,
    homeIndex: Int,
) {
    val isPagerBackHandlerEnabled by remember(homeIndex) {
        derivedStateOf {
            navigator.current() is Route.Main &&
                    navigator.backStackSize() == 1 &&
                    mainState.selectedPage != homeIndex
        }
    }
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = { mainState.animateToPage(homeIndex) },
    )
}
