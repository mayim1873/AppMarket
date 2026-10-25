package com.app.market.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.market.resources.Res
import com.app.market.resources.about_open_source_licenses
import com.app.market.resources.about_source_code
import com.app.market.resources.about_title
import com.app.market.resources.app_name
import com.app.market.resources.ic_launcher
import com.app.market.ui.component.CardSegmentContainer
import com.app.market.ui.component.MarketDivider
import com.app.market.ui.component.MarketScaffold
import com.app.market.ui.component.PageVerticalPadding
import com.app.market.ui.component.SectionTitle
import com.app.market.ui.component.SettingsArrowRow
import com.app.market.ui.component.SettingsGroup
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 项目源代码地址。 */
private const val SourceCodeUrl = "https://github.com/mayim1873/AppMarket"

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

/**
 * 关于页：参照 KernelSU 的 Material 版式——大标题顶栏 + 居中的应用图标 / 名称 / 版本，
 * 下方是「项目地址」与「开源许可」两组分段列表。
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    MarketScaffold(
        title = stringResource(Res.string.about_title),
        onBack = onBack,
    ) { innerPadding, _, scrollBehavior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + PageVerticalPadding,
                bottom = innerPadding.calculateBottomPadding() + PageVerticalPadding,
            ),
        ) {
            item(key = "hero") {
                AboutHero()
            }

            item(key = "source-code") {
                SettingsGroup {
                    SettingsArrowRow(
                        title = stringResource(Res.string.about_source_code),
                        summary = SourceCodeUrl.removePrefix("https://"),
                        onClick = { onOpenUrl(SourceCodeUrl) },
                    )
                }
            }

            item(key = "licenses-title") {
                SectionTitle(text = stringResource(Res.string.about_open_source_licenses))
            }

            // 多行大卡片拆成独立 lazy item，用 CardSegmentContainer 拼出视觉连续的卡片。
            // key 必须唯一：不同条目可能指向同一个地址（如 Core 与 SplashScreen），
            // 所以用「标题 + 序号」而不是 URL。
            itemsIndexed(
                OpenSourceLicenses,
                key = { index, license -> "${license.title}#$index" },
            ) { index, license ->
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
}

/**
 * 应用信息头部：居中的图标、名称与版本号（KernelSU 关于页同款结构）。
 */
@Composable
private fun AboutHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_launcher),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
        Text(
            text = stringResource(Res.string.app_name),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "v${misc.VersionInfo.VERSION_NAME} (${misc.VersionInfo.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
