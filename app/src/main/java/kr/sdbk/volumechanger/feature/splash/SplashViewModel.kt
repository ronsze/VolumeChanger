package kr.sdbk.volumechanger.feature.splash

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.volumechanger.base.BaseViewModel
import kr.sdbk.volumechanger.data.repository.LocationRepository
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val geofenceModule: GeofenceModule,
    private val ioDispatcher: CoroutineDispatcher
): BaseViewModel() {
    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            val res = withContext(ioDispatcher) { runCatching { locationRepository.getAllLocation() } }
            res.onSuccess {
                refreshGeofencing(it)
            }
            res.onFailure {
                _uiState.set(SplashUiState.Failed(it.message.toString()))
            }
            withContext(Dispatchers.Default) { delay(2000) }
            _uiState.set(SplashUiState.Loaded)
        }
    }

    private fun refreshGeofencing(
        locationList: List<LocationEntity>
    ) {
        geofenceModule.addGeofencing(
            locationEntityList = locationList,
            onSuccess = {
                _uiState.set(SplashUiState.Loaded)
            },
            onFailure = {
                _uiState.set(SplashUiState.Failed("Register geofencing failed"))
            }
        )
    }

    sealed interface SplashUiState {
        data object Loading: SplashUiState
        data object Loaded: SplashUiState
        data class Failed(val message: String): SplashUiState
    }
}