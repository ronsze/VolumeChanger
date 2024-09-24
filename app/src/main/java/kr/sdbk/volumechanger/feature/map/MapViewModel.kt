package kr.sdbk.volumechanger.feature.map

import android.location.Location
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.volumechanger.base.AlertState
import kr.sdbk.volumechanger.base.BaseViewModel
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toLatLng
import kr.sdbk.volumechanger.data.repository.LocationRepository
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.di.DefaultDispatcher
import kr.sdbk.volumechanger.di.IODispatcher
import kr.sdbk.volumechanger.util.Constants
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val locationRepository: LocationRepository
): BaseViewModel() {
    companion object {
        private const val DISTANCE_SPACE = 25f      // meter
    }

    private val _locationList: MutableStateFlow<List<LocationEntity>> = MutableStateFlow(listOf())
    val locationList get() = _locationList.asStateFlow()

    val alertState: MutableStateFlow<AlertState> = MutableStateFlow(AlertState())

    fun loadLocation() {
        viewModelScope.launch {
            val res = runCatching { withContext(ioDispatcher) { locationRepository.getAllLocation() } }
            res.onSuccess {
                _locationList.set(it)
            }
            res.onFailure {
                alertState.set(AlertState(true, it.message ?: Constants.UNKNOWN_ERROR))
            }
        }
    }

    fun insertLocation(location: LocationEntity) {
        viewModelScope.launch {
            val isOverlap = withContext(defaultDispatcher) { checkOverlap(location) }
            if (isOverlap) {
                alertState.set(AlertState(true, "It overlaps"))
            } else {
                val res = runCatching { withContext(ioDispatcher) { locationRepository.insertLocation(location) } }
                basicProcessing("Inserted", res)
            }
        }
    }

    fun updateLocation(location: LocationEntity) {
        viewModelScope.launch {
            val res = runCatching { withContext(ioDispatcher) { locationRepository.updateLocation(location) } }
            basicProcessing("Updated", res)
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            val res = runCatching { withContext(ioDispatcher) { locationRepository.deleteLocation(location) } }
            basicProcessing("Deleted", res)
        }
    }

    private fun<R> basicProcessing(message: String, res: Result<R>) {
        res.onSuccess {
            if (message.isNotEmpty()) {
                alertState.set(AlertState(true, message))
                loadLocation()
            }
        }
        res.onFailure {
            alertState.set(AlertState(true, it.message ?: Constants.UNKNOWN_ERROR))
        }
    }

    private fun checkOverlap(input: LocationEntity): Boolean {
        val newLocation = Location("new").apply {
            latitude = input.location.first
            longitude = input.location.second
        }
        locationList.value.forEach { old ->
            val oldLocation = Location("new").apply {
                latitude = old.location.first
                longitude = old.location.second
            }
            val distance = newLocation.distanceTo(oldLocation)

            if (distance + DISTANCE_SPACE > input.range + old.range) return true
        }
        return false
    }

    fun resetAlertState() {
        alertState.set(AlertState())
    }
}