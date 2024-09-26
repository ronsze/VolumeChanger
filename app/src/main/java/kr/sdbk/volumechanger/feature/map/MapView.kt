package kr.sdbk.volumechanger.feature.map

import android.graphics.Paint.Align
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    val alertState by viewModel.alertState.collectAsStateWithLifecycle()
    val defaultAddLocationDialogState = AddLocationDialogState(currentLocation = LatLng(0.0, 0.0))
    var addLocationDialogState: AddLocationDialogState by remember { mutableStateOf(defaultAddLocationDialogState) }
    Box {
        val cameraPositionState = rememberCameraPositionState {
            val initialLocation: LatLng = selectedLocation?.location?.toLatLng() ?: LocationConverter.locationStringToPair(Values.DEFAULT_LOCATION).toLatLng()
            position = CameraPosition.fromLatLngZoom(initialLocation, Values.DEFAULT_ZOOM)
        }

        val locationList by viewModel.locationList.collectAsStateWithLifecycle()

        Map(
            cameraPositionState = cameraPositionState,
            locationList = locationList
        ) {
            addLocationDialogState = AddLocationDialogState(
                isVisible = true,
                currentLocation = it
            )
        }

        Tools(

        )

        if (alertState.isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 15.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = alertState.message,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

        }
    }

    if (addLocationDialogState.isVisible) {
        AddLocationDialog(
            locationEntity = addLocationDialogState.locationEntity,
            currentLocation = addLocationDialogState.currentLocation,
            onClickConfirm = { viewModel.insertLocation(it) },
            onDismissRequest = { addLocationDialogState = defaultAddLocationDialogState }
        )
    }
}

@Composable
private fun Map(
    cameraPositionState: CameraPositionState,
    locationList: List<LocationEntity>,
    showAddLocationDialog: (LatLng) -> Unit
) {
    val properties = MapProperties(
        mapType = MapType.TERRAIN
    )
    GoogleMap(
        properties = properties,
        cameraPositionState = cameraPositionState,
        onMapLongClick = showAddLocationDialog,
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
    val markerState = MarkerState(position = locationEntity.location.toLatLng())
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

data class AddLocationDialogState(
    val isVisible: Boolean = false,
    val locationEntity: LocationEntity? = null,
    val currentLocation: LatLng
)