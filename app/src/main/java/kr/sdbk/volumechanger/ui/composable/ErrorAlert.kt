package kr.sdbk.volumechanger.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kr.sdbk.volumechanger.R

@Composable
fun ErrorAlert(
    message: String,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        text = {
            BaseText(
                text = message,
                fontSize = 16.sp
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            BaseText(
                text = stringResource(id = R.string.confirm),
                modifier = Modifier.clickable { onDismissRequest() }
            )
        },
        dismissButton = {
            BaseText(
                text = stringResource(id = R.string.retry),
                modifier = Modifier.clickable { onRetry() }
            )
        }
    )
}

@Preview
@Composable
private fun Preview() {
    ErrorAlert(message = "Unknown error occurred", onDismissRequest = {}) {}
}