package kr.sdbk.volumechanger.feature.map

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

@Composable
fun MapView(
    selectedLocation: LocationEntity?,
    viewModel: MapViewModel = hiltViewModel()
) {
}