package kr.sdbk.volumechanger.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kr.sdbk.volumechanger.data.mapper.LocationConverter
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toLatLng
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.util.Values

@Composable
fun MapView(
    selectedLocation: LocationEntity?,
    viewModel: MapViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) viewModel.loadLocation()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box {
        val cameraPositionState = rememberCameraPositionState {
            val initialLocation: LatLng = selectedLocation?.location?.toLatLng() ?: LocationConverter.locationStringToPair(Values.DEFAULT_LOCATION).toLatLng()
            position = CameraPosition.fromLatLngZoom(initialLocation, 10f)
        }

        val locationList by viewModel.locationList.collectAsStateWithLifecycle()

        Map(
            cameraPositionState = cameraPositionState,
            locationList = locationList
        )

        Tools(

        )
    }
}

@Composable
private fun Map(
    cameraPositionState: CameraPositionState,
    locationList: List<LocationEntity>
) {
    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        locationList.forEach { 
            LocationMarker(locationEntity = it)
        }
    }
}

@Composable
private fun LocationMarker(
    locationEntity: LocationEntity
) {

}

@Composable
private fun Tools() {

}