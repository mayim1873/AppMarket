# AppMarket

Kotlin Multiplatform（Android + Desktop）应用商店客户端，UI 使用 Compose Multiplatform + **Material 3 Expressive**（`androidx.compose.material3`，`MaterialExpressiveTheme`）。

## UI 设计规范：主题

主题入口：[MarketTheme.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/theme/MarketTheme.kt)

- **颜色**：`MaterialExpressiveTheme` + 品牌高彩度色板（[MarketColorScheme.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/theme/MarketColorScheme.kt)）。Android 12+ 优先使用系统动态取色（Material You），`rememberPlatformColorScheme` 为 expect/actual 实现；桌面端回退品牌色板。界面颜色一律取 `MaterialTheme.colorScheme.*`，禁止硬编码色值。
- **字体**：M3E 层级（[MarketTypography.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/theme/MarketTypography.kt)）。大标题 Bold + 收紧字距，标签 SemiBold。文字样式一律取 `MaterialTheme.typography.*`。
- **形状**：大圆角是 M3E 的标志——卡片 28dp、内嵌卡片 20dp、按钮/标签胶囊形（[MarketShapes.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/theme/MarketShapes.kt)）。手搓圆角时引用 `MarketShapeTokens`，不要写字面量。
- **动效**：`MotionScheme.expressive()`（弹簧驱动）。页面转场用 `AnimatedContent` 的共享轴位移 + 淡入淡出。

## UI 设计规范：垂直间距

全局间距体系（常量定义在 [MarketScaffold.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/component/MarketScaffold.kt)）：

- **页面出入距 `PageVerticalPadding` = 12dp**：滚动页面「顶栏底部 → 内容顶部」「内容末尾 → 页面底部」的额外间距，一律引用该常量。
  - 出入距统一通过列表 `contentPadding` 实现（顶部 `innerPadding.calculateTopPadding() + PageVerticalPadding`、底部 `bottomPadding + PageVerticalPadding`）。
  - `bottomPadding: Dp` 参数只出现在主 Tab 页签名里（外层 MainScreen 持有悬浮导航栏，需透传其高度）；二级推入页一律用脚手架自身的 `innerPadding`，签名不带 `bottomPadding`。
- **卡片 / 区块间距 = 20dp**：网格 `spacedBy`、块间距、区块标题上方间距。列表内卡片间距用 12dp。
- **页面水平内边距 = 16dp**：卡片贴边距 16dp；`SectionTitle` 用 20dp 与卡片内容形成轻微缩进层次。
- **小标题 `SectionTitle`**：上方 20dp 由标题自身 `topPadding` 提供；标题到内容 8dp；水平 20dp。紧贴顶栏时传 `topPadding = 0.dp`。
- **卡片自身不携带外部垂直边距**：垂直节奏由列表 spacing 提供。

## UI 规范：通用模式

- **所有 UI 使用 material3**，不再依赖任何 miuix 组件。新增界面优先复用 `ui/component/*` 中的封装。
- **脚手架**：二级页用 `MarketScaffold(title, onBack)`；主页签用 `MainTabScaffold(title)`。两者都提供 `(innerPadding/topPadding, backdropModifier 占位, scrollBehavior)` 回调，列表挂 `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`。
- **顶栏**：`LargeTopAppBar`（窄屏可折叠）/ `TopAppBar`（宽屏固定），由脚手架统一处理，不手搓。
- **卡片**：`MarketCard`（28dp 圆角、`surfaceContainerLow` 容器色、无阴影靠色调分层）；被拆成多个 lazy item 的连续卡片用 `CardSegmentContainer(isFirst, isLast)` + `MarketDivider`。
- **返回按钮统一** `Icons.AutoMirrored.Filled.ArrowBack`（脚手架内已封装）。
- **图标**：`androidx.compose.material.icons`（`Icons.Filled.*` / `Icons.AutoMirrored.*`）。
- **宽屏适配**：以 `rememberIsWideScreen()` 为唯一判定；宽屏用 `MarketNavigationRail`，窄屏用 `MarketNavigationBar`（M3E `ShortNavigationBar` 悬浮胶囊导航）。
- **多行卡片拆独立 lazy item**：`CardSegmentContainer` 拼视觉连续卡片，每行独立 item 保证滚动性能。
- **设置页**：用 `SettingsGroup` / `SettingsRow` / `SettingsSwitchRow` / `SettingsArrowRow` / `SettingsDropdownRow`，不要手写行布局。
- **长内容 Dialog**：`MarketDialog` 内容用 `heightIn(max = …)` 限高 + 内部 `verticalScroll`，按钮固定底部；确认类弹窗优先 `MarketConfirmDialog`。
- **用户反馈**：轻量操作结果用 `uiPlatform.showToast(message)`。
- **i18n**：所有用户可见字符串走 Compose Resources（`stringResource(Res.string.xxx)`），禁止硬编码；日志消息英文，代码注释中文。
- **Flow 收集**：屏幕一律 `collectAsStateWithLifecycle()`，不用 `collectAsState()`。
- **可复用组件 API**：`ui/component/*` 的可复用 composable 暴露 `modifier: Modifier = Modifier` 作为第一个可选参数，并应用到根节点。

## UI 设计规范：按钮

- 全部按钮使用 [AppButton.kt](app/shared/src/commonMain/kotlin/com/app/market/ui/component/AppButton.kt) 中的 `AppButton` / `AppTextButton` / `AppCompactButton` / `AppActionButton`，不要直接使用 material3 的 `Button` / `TextButton`。
- 按钮配色用 `AppButtonDefaults.filled()` / `neutral()` / `tonal()` / `text()`，不要引用 material3 的 `ButtonDefaults.colors(...)`。
- 按钮文字统一经 `AppButtonText`：`labelLarge`（14sp），超宽时自动缩字至最小 8sp 再省略号。
- 调整按钮样式只改 AppButton.kt 一处。

## 构建验证

- 快速编译检查（commonMain + desktop）：`.\gradlew.bat :app:shared:compileKotlinDesktop`
- Android 目标：`.\gradlew.bat :app:shared:compileAndroidMain`
