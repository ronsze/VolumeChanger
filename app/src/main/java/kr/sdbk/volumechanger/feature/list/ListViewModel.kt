package kr.sdbk.volumechanger.feature.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.volumechanger.base.BaseViewModel
import kr.sdbk.volumechanger.data.repository.LocationRepository
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.di.IODispatcher
import kr.sdbk.volumechanger.util.Constants
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val locationRepository: LocationRepository
): BaseViewModel() {
    private val _uiState: MutableStateFlow<ListUiState> = MutableStateFlow(ListUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        _uiState.set(ListUiState.Loading)
        viewModelScope.launch {
            val res = withContext(ioDispatcher) {
                runCatching {
                    locationRepository.getAllLocation()
                }
            }
            res.onSuccess {
                _uiState.set(ListUiState.Loaded(it))
            }
            res.onFailure {
                _uiState.set(ListUiState.Failed(it.message ?: Constants.UNKNOWN_ERROR))
            }
        }
    }

    sealed interface ListUiState {
        data object Loading: ListUiState
        data class Loaded(val list: List<LocationEntity>): ListUiState
        data class Failed(val message: String): ListUiState
    }
}