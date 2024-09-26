package kr.sdbk.volumechanger.util.enums

import androidx.annotation.StringRes
import kr.sdbk.volumechanger.R

enum class BellVolume(
    @StringRes val label: Int,
    val value: Int
) {
    MUTE(R.string.mute, -1),
    VIBRATION(R.string.vibration, 0),
    MAX(R.string.max, 100)
}

enum class MediaVolume(
    @StringRes val label: Int,
    val value: Int
) {
    MUTE(R.string.mute, 0),
    MAX(R.string.max, 100)
}