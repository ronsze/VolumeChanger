package kr.sdbk.volumechanger.util

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

fun Pair<Double, Double>.toLatLng() = LatLng(first, second)
fun LatLng.toPair() = Pair(latitude, longitude)

fun locationToAddress(
    context: Context,
    location: LatLng
): String {
    val res = Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
    return res?.first()?.getAddressLine(0) ?: ""
}