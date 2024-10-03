package kr.sdbk.volumechanger.feature.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.volumechanger.base.AlertState
import kr.sdbk.volumechanger.base.BaseViewModel
import kr.sdbk.volumechanger.data.model.Location
import kr.sdbk.volumechanger.data.repository.LocationRepository
import kr.sdbk.volumechanger.di.DefaultDispatcher
import kr.sdbk.volumechanger.di.IODispatcher
import kr.sdbk.volumechanger.util.Constants
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val locationRepository: LocationRepository,
    private val geofenceModule: GeofenceModule
): BaseViewModel() {
    private val _uiState: MutableStateFlow<ListUiState> = MutableStateFlow(ListUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    val alertState: MutableStateFlow<AlertState> = MutableStateFlow(AlertState())

    fun loadLocation() {
        _uiState.set(ListUiState.Loading)
        viewModelScope.launch {
            val res = withContext(ioDispatcher) { runCatching { locationRepository.getAllLocation() } }
            res.onSuccess {
                _uiState.set(ListUiState.Loaded(it))
            }
            res.onFailure {
                _uiState.set(ListUiState.Failed(it.message ?: Constants.UNKNOWN_ERROR))
            }
        }
    }

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            val res = runCatching {
                withContext(ioDispatcher) {
                    locationRepository.updateLocation(location)
                    if (location.enabled) geofenceModule.addGeofencing(listOf(location))
                    else geofenceModule.removeGeofencing(listOf(location))
                }
            }
            res.onSuccess {
                loadLocation()
            }
            res.onFailure {
                showAlert(it.message ?: Constants.UNKNOWN_ERROR)
            }
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            val res = runCatching { withContext(ioDispatcher) { locationRepository.deleteLocation(location) } }
            res.onSuccess {
                showAlert("Deleted")
                loadLocation()
            }
            res.onFailure {
                showAlert(it.message ?: Constants.UNKNOWN_ERROR)
            }
        }
    }

    private fun showAlert(message: String) {
        viewModelScope.launch {
            alertState.emit(AlertState(true, message))
            delay(3000)
            alertState.emit(AlertState())
        }
    }

    sealed interface ListUiState {
        data object Loading: ListUiState
        data class Loaded(val list: List<Location>): ListUiState
        data class Failed(val message: String): ListUiState
    }
}