package com.app.market.platform

import androidx.compose.runtime.Composable

expect fun isPredictiveBackSupported(): Boolean

@Composable
expect fun ApplyPredictiveBackPreference(enabled: Boolean)
