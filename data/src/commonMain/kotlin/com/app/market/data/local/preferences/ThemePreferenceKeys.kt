package com.app.market.data.local.preferences

import com.app.market.data.local.BooleanPreferenceKey
import com.app.market.data.local.StringPreferenceKey

object ThemePreferenceKeys {
    private const val NS = "theme_preferences"
    val EnableNavigationBadge = BooleanPreferenceKey(NS, "enable_navigation_badge", true)
    val NavRailExpanded = BooleanPreferenceKey(NS, "nav_rail_expanded")
    val EnablePredictiveBack = BooleanPreferenceKey(NS, "enable_predictive_back")
    val PageScale = StringPreferenceKey(NS, "page_scale")
}
