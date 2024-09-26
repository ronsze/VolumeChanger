package kr.sdbk.volumechanger.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kr.sdbk.volumechanger.data.mapper.LocationConverter
import kr.sdbk.volumechanger.util.toLatLng
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

    var isAddLocationDialogVisible by remember { mutableStateOf(true) }
    Box {
        val cameraPositionState = rememberCameraPositionState {
            val initialLocation: LatLng = selectedLocation?.location?.toLatLng() ?: LocationConverter.locationStringToPair(Values.DEFAULT_LOCATION).toLatLng()
            position = CameraPosition.fromLatLngZoom(initialLocation, Values.DEFAULT_ZOOM)
        }

        val locationList by viewModel.locationList.collectAsStateWithLifecycle()

        Map(
            cameraPositionState = cameraPositionState,
            locationList = locationList
        )

        Tools(

        )
    }

    if (isAddLocationDialogVisible) {
        AddLocationDialog(
            locationEntity = null,
            currentLocation = LocationConverter.locationStringToPair(Values.DEFAULT_LOCATION).toLatLng(),
            onClickConfirm = {},
            onDismissRequest = { isAddLocationDialogVisible = false }
        )
    }
}

@Composable
private fun Map(
    cameraPositionState: CameraPositionState,
    locationList: List<LocationEntity>
) {
    val properties = MapProperties(
        mapType = MapType.TERRAIN
    )
    GoogleMap(
        properties = properties,
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
    val markerState = MarkerState(position = LocationConverter.locationStringToPair(Values.DEFAULT_LOCATION).toLatLng())
    LaunchedEffect(key1 = markerState) {
        markerState.showInfoWindow()
    }

    Marker(
        state = markerState,
        title = locationEntity.name,
        snippet = "${locationEntity.range}M",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
    )
}

@Composable
private fun Tools() {

}