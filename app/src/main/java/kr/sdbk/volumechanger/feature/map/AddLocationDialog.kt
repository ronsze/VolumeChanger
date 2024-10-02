package kr.sdbk.volumechanger.feature.map

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.sdbk.volumechanger.R
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.ui.composable.BaseText
import kr.sdbk.volumechanger.ui.theme.Purple80
import kr.sdbk.volumechanger.util.Values
import kr.sdbk.volumechanger.util.enums.BellVolume
import kr.sdbk.volumechanger.util.enums.MediaVolume
import kr.sdbk.volumechanger.util.locationToAddress
import kr.sdbk.volumechanger.util.toLatLng
import kr.sdbk.volumechanger.util.toPair

@Composable
fun AddLocationDialog(
    locationEntity: LocationEntity?,
    currentLocation: LatLng,
    onClickConfirm: (LocationEntity) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(25.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                var name by remember { mutableStateOf(locationEntity?.name ?: "") }
                var address by remember { mutableStateOf("-") }
                var range by remember { mutableStateOf(locationEntity?.range ?: Values.RANGE_ARRAY.first()) }
                var bellVolume by remember { mutableStateOf(locationEntity?.bellVolume ?: 0) }
                var mediaVolume by remember { mutableStateOf(locationEntity?.mediaVolume ?: 0) }

                LaunchedEffect(Unit) {
                    scope.launch(Dispatchers.IO) {
                        address = locationToAddress(
                            context = context,
                            location = locationEntity?.location?.toLatLng() ?: currentLocation
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { BaseText(text = stringResource(id = R.string.name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Container(label = stringResource(id = R.string.address)) {
                    BaseText(
                        text = address,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Container(label = stringResource(id = R.string.range)) {
                    RangeSelector(range = range) {
                        range = it
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Container(label = stringResource(id = R.string.bell_volume)) {
                    BellVolumeController(bellVolume = bellVolume) {
                        bellVolume = it
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Container(label = stringResource(id = R.string.media_volume)) {
                    MediaVolumeController(mediaVolume = mediaVolume) {
                        mediaVolume = it
                    }
                }
                Spacer(modifier = Modifier.height(35.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Cyan
                    ),
                    enabled = name.isNotEmpty(),
                    shape = RoundedCornerShape(35.dp),
                    onClick = {
                        val entity = LocationEntity(
                            created = locationEntity?.created ?: System.currentTimeMillis(),
                            name = name,
                            location = currentLocation.toPair(),
                            range = range,
                            bellVolume = bellVolume,
                            mediaVolume = mediaVolume
                        )
                        onClickConfirm(entity)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                ) {
                    BaseText(
                        text = stringResource(id = R.string.confirm),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun Container(
    label: String,
    content: @Composable () -> Unit
) {
    BaseText(
        text = label,
        color = Color.LightGray
    )
    Spacer(modifier = Modifier.height(5.dp))

    content()
}

@Composable
private fun RangeSelector(
    range: Int,
    onClickRange: (Int) -> Unit
) {
    var expended by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(100.dp)
            .clickable { expended = true }
    ) {
        BaseText(
            text = "${range}M",
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
        )

        Image(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "",
            modifier = Modifier.size(24.dp)
        )
    }

    DropdownMenu(
        expanded = expended,
        onDismissRequest = { expended = false }
    ) {
        Values.RANGE_ARRAY.filter { it != range }.forEach {
            DropdownMenuItem(
                text = {
                    BaseText(
                        text = "${it}M",
                        fontSize = 16.sp
                    )
                },
                onClick = {
                    onClickRange(it)
                    expended = false
                }
            )
        }

    }
}

@Composable
private fun BellVolumeController(
    bellVolume: Int,
    onValueChange: (Int) -> Unit
) {
    val selectedVolume: BellVolume? = BellVolume.entries.firstOrNull { vol -> vol.value == bellVolume }
    Slider(
        value = if (bellVolume == BellVolume.MUTE.value) 0f else bellVolume.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = 0f .. 100f,
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 10.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
    ) {
        BellVolume.entries.forEach {
            VolumeButton(label = it.label, isSelected = selectedVolume == it) {
                onValueChange(it.value)
            }
        }
    }
}

@Composable
private fun MediaVolumeController(
    mediaVolume: Int,
    onValueChange: (Int) -> Unit
) {
    val selectedVolume: MediaVolume? = MediaVolume.entries.firstOrNull { vol -> vol.value == mediaVolume }
    Slider(
        value = if (mediaVolume == MediaVolume.MUTE.value) 0f else mediaVolume.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = 0f .. 100f,
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 10.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
    ) {
        MediaVolume.entries.forEach {
            VolumeButton(label = it.label, isSelected = selectedVolume == it) {
                onValueChange(it.value)
            }
        }
    }
}

@Composable
private fun RowScope.VolumeButton(
    @StringRes label: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (background, border) = if (isSelected) {
        Purple80 to null
    } else {
        Color.Transparent to BorderStroke(1.dp, Color.Black)
    }
    Button(
        shape = RoundedCornerShape(32.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = background
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        modifier = Modifier.weight(1f),
    ) {
        BaseText(
            text = stringResource(id = label),
            fontSize = 12.sp
        )
    }
}

@Preview(backgroundColor = 0xFFFFFF, showBackground = true)
@Composable
private fun Preview() {
    AddLocationDialog(
        null,
        LatLng(0.0, 0.0),
        {},
        {}
    )
}