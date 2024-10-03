package kr.sdbk.volumechanger.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BasicAlert(
    message: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 15.dp)
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = message,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}