package com.app.market.platform

import androidx.compose.runtime.Composable

actual fun isPredictiveBackSupported(): Boolean = false

@Composable
actual fun ApplyPredictiveBackPreference(enabled: Boolean) = Unit
