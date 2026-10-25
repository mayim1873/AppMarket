package com.app.market.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.market.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class ThemeSettingsUiState(
    val enableNavigationBadge: Boolean = true,
    val enablePredictiveBack: Boolean = false,
    val pageScale: Float = 1f,
)

class ThemeSettingsViewModel(
    private val preferences: ThemePreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        preferences.enableNavigationBadge,
        preferences.enablePredictiveBack,
        preferences.pageScale,
    ) { badge, predictiveBack, scale ->
        ThemeSettingsUiState(
            enableNavigationBadge = badge,
            enablePredictiveBack = predictiveBack,
            pageScale = scale,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettingsUiState())

    fun setEnableNavigationBadge(value: Boolean) = persist { preferences.setEnableNavigationBadge(value) }
    fun setEnablePredictiveBack(value: Boolean) = persist { preferences.setEnablePredictiveBack(value) }
    fun setPageScale(value: Float) = persist { preferences.setPageScale(value) }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
