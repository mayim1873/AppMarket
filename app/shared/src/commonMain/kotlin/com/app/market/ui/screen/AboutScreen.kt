package com.app.market.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.market.resources.Res
import com.app.market.resources.about_open_source_licenses
import com.app.market.resources.about_title
import com.app.market.resources.app_name
import com.app.market.resources.back
import com.app.market.resources.ic_launcher
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.component.SettingsArrowRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 顶栏标题开始淡入的滚动距离。 */
private val TopBarCollapseDistance = 160.dp

/** 开源许可条目：标题 + 项目地址（摘要由地址去掉协议头得到）。 */
private data class OpenSourceLicense(
    val title: String,
    val url: String,
) {
    val summary: String get() = url.removePrefix("https://")
}

private val OpenSourceLicenses = listOf(
    OpenSourceLicense("AndroidX Activity", "https://developer.android.com/jetpack/androidx/releases/activity"),
    OpenSourceLicense("AndroidX Core", "https://developer.android.com/jetpack/androidx/releases/core"),
    OpenSourceLicense("AndroidX DataStore", "https://developer.android.com/jetpack/androidx/releases/datastore"),
    OpenSourceLicense("AndroidX Lifecycle", "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
    OpenSourceLicense(
        "AndroidX Navigation Event",
        "https://developer.android.com/jetpack/androidx/releases/navigationevent",
    ),
    OpenSourceLicense("AndroidX SplashScreen", "https://developer.android.com/jetpack/androidx/releases/core"),
    OpenSourceLicense("coil", "https://github.com/coil-kt/coil"),
    OpenSourceLicense("Compose Media Player", "https://github.com/kdroidFilter/ComposeMediaPlayer"),
    OpenSourceLicense("Compose Multiplatform", "https://github.com/JetBrains/compose-multiplatform"),
    OpenSourceLicense("HDiffPatch", "https://github.com/sisong/HDiffPatch"),
    OpenSourceLicense("HiddenApiBypass", "https://github.com/LSPosed/AndroidHiddenApiBypass"),
    OpenSourceLicense("HyperNotification", "https://github.com/xzakota/HyperNotification"),
    OpenSourceLicense("Koin", "https://github.com/InsertKoinIO/koin"),
    OpenSourceLicense("kotlinx-datetime", "https://github.com/Kotlin/kotlinx-datetime"),
    OpenSourceLicense("kotlinx.collections.immutable", "https://github.com/Kotlin/kotlinx.collections.immutable"),
    OpenSourceLicense("kotlinx.coroutines", "https://github.com/Kotlin/kotlinx.coroutines"),
    OpenSourceLicense("kotlinx.serialization", "https://github.com/Kotlin/kotlinx.serialization"),
    OpenSourceLicense("ktor", "https://github.com/ktorio/ktor"),
    OpenSourceLicense(
        "Material 3 Expressive",
        "https://developer.android.com/jetpack/androidx/releases/compose-material3",
    ),
    OpenSourceLicense("Shizuku", "https://github.com/RikkaApps/Shizuku"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val collapseDistancePx = with(LocalDensity.current) { TopBarCollapseDistance.toPx() }
    // 逐帧变化的滚动进度只以 State 传递，读取下沉到 graphicsLayer / drawBehind
    val collapseProgress = remember(lazyListState, collapseDistancePx) {
        derivedStateOf {
            when {
                lazyListState.firstVisibleItemIndex > 0 -> 1f
                else -> (lazyListState.firstVisibleItemScrollOffset / collapseDistancePx)
                    .coerceIn(0f, 1f)
            }
        }
    }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AboutTopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior,
                collapseProgress = collapseProgress,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AboutContent(
            innerPadding = innerPadding,
            bottomInset = bottomInset,
            lazyListState = lazyListState,
            scrollBehavior = scrollBehavior,
            onOpenUrl = onOpenUrl,
        )
    }
}

/**
 * 沉浸式顶栏：hero 面板上保持透明，滚动后自行绘制 surface 遮罩并淡入标题。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutTopBar(
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    collapseProgress: State<Float>,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onSurface
    val scrimColor = MaterialTheme.colorScheme.scrim
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.about_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                modifier = Modifier.graphicsLayer { alpha = collapseProgress.value },
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(scrimColor.copy(alpha = 0.32f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = Color.White,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = titleColor,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.drawBehind {
            val progress = collapseProgress.value
            if (progress > 0f) {
                drawRect(surfaceColor.copy(alpha = progress))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    innerPadding: PaddingValues,
    bottomInset: Dp,
    lazyListState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenUrl: (String) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            // hero 面板铺满到状态栏下面
            top = 0.dp,
            bottom = bottomInset + PageVerticalPadding,
        ),
    ) {
        item(key = "hero") {
            AboutHero()
        }

        item(key = "licenses-title") {
            SectionTitle(text = stringResource(Res.string.about_open_source_licenses))
        }

        // 多行大卡片拆成独立 lazy item，用 CardSegmentContainer 拼出视觉连续的卡片。
        // key 必须唯一：不同条目可能指向同一个地址（如 Core 与 SplashScreen），
        // 所以用「序号 + 标题」而不是 URL。
        itemsIndexed(OpenSourceLicenses, key = { index, license -> "${license.title}#$index" }) { index, license ->
            CardSegmentContainer(
                isFirst = index == 0,
                isLast = index == OpenSourceLicenses.lastIndex,
            ) {
                SettingsArrowRow(
                    title = license.title,
                    summary = license.summary,
                    onClick = { onOpenUrl(license.url) },
                )
                if (index != OpenSourceLicenses.lastIndex) {
                    MarketDivider(startPadding = 16.dp)
                }
            }
        }
    }
}

/**
 * 关于页 hero：品牌色渐变面板 + 应用图标 + 名称 / 版本。
 */
@Composable
private fun AboutHero(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            // 顶部预留顶栏高度，避免图标被返回按钮压住
            .padding(top = 88.dp, bottom = 36.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = "icon",
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.app_name),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = 0.sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "v${misc.VersionInfo.VERSION_NAME} (${misc.VersionInfo.VERSION_CODE})",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
