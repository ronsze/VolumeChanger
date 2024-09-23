package kr.sdbk.volumechanger.feature.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.sdbk.volumechanger.base.BaseViewModel

class SplashViewModel: BaseViewModel() {
    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            delay(2000)
            _uiState.set(SplashUiState.Loaded)
        }
    }

    sealed interface SplashUiState {
        data object Loading: SplashUiState
        data object Loaded: SplashUiState
        data class Failed(val message: String): SplashUiState
    }
}