package com.app.market.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.json.Json

/**
 * 轻量返回栈：以 [SnapshotStateList] 保存路由，Compose 可直接观察其变化。
 * 应用只有线性导航，无需引入额外的导航库。
 */
class Navigator(
    val backStack: SnapshotStateList<Route> = mutableStateListOf(Route.Main),
) {
    fun push(route: Route) {
        if (route !in backStack) {
            backStack.add(route)
        }
    }

    fun replace(route: Route) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = route
        } else {
            backStack.add(route)
        }
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popUntil(predicate: (Route) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current(): Route? = backStack.lastOrNull()

    fun backStackSize(): Int = backStack.size
}

private val RouteJson = Json { ignoreUnknownKeys = true }

/** 返回栈的可保存化：把路由序列化为 JSON 字符串，配置变更 / 进程重建后仍能恢复。 */
private val NavigatorSaver: Saver<SnapshotStateList<Route>, List<String>> = Saver(
    save = { stack -> stack.map { RouteJson.encodeToString(Route.serializer(), it) } },
    restore = { encoded ->
        mutableStateListOf<Route>().apply {
            encoded.forEach { add(RouteJson.decodeFromString(Route.serializer(), it)) }
        }
    },
)

@Composable
fun rememberNavigator(): Navigator {
    val backStack = rememberSaveable(saver = NavigatorSaver) {
        mutableStateListOf<Route>(Route.Main)
    }
    return remember(backStack) { Navigator(backStack) }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided")
}
