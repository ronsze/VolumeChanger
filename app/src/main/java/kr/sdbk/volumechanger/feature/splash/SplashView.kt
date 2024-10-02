package kr.sdbk.volumechanger.feature.splash

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kr.sdbk.volumechanger.R
import kr.sdbk.volumechanger.base.AlertState
import kr.sdbk.volumechanger.ui.composable.BaseText
import kr.sdbk.volumechanger.ui.composable.ErrorAlert
import kotlin.system.exitProcess

@Composable
fun SplashView(
    navigateToList: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var isPermissionDeniedAlertVisible by remember { mutableStateOf(false) }
    var errorAlertState by remember { mutableStateOf(AlertState()) }
    if (uiState is SplashViewModel.SplashUiState.Failed) errorAlertState = AlertState(true, uiState.message)

    RequestPermissions(
        onPermissionsGranted = viewModel::loadData,
        onPermissionsDenied = { isPermissionDeniedAlertVisible = true }
    )

    LaunchedEffect(key1 = uiState) {
        if (uiState == SplashViewModel.SplashUiState.Loaded) navigateToList()
    }
    
    Content()

    if (isPermissionDeniedAlertVisible) {
        AlertDialog(
            text = { BaseText(text = stringResource(id = R.string.please_grant_permissions)) },
            onDismissRequest = {},
            confirmButton = { 
                BaseText(
                    text = stringResource(id = R.string.confirm),
                    modifier = Modifier.clickable { exitProcess(0) }
                )
            }
        )
    }

    if (errorAlertState.isVisible) {
        ErrorAlert(
            message = errorAlertState.message,
            onDismissRequest = { errorAlertState = AlertState() },
            onRetry = viewModel::loadData
        )
    }
}

@Composable
private fun Content() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_loading),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestPermissions(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val locationPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val permissionsState = rememberMultiplePermissionsState(permissions = locationPermissions)

    LaunchedEffect(key1 = permissionsState) {
        if (!permissionsState.allPermissionsGranted) permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) RequestBackgroundLocationPermission(
        onPermissionsGranted = onPermissionsGranted,
        onPermissionsDenied = onPermissionsDenied
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestBackgroundLocationPermission(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    LaunchedEffect(key1 = permissionState) {
        if (!permissionState.status.isGranted) permissionState.launchPermissionRequest()
    }

    if (permissionState.status.isGranted) RequestNotificationPolicyAccessPermission(
        onPermissionsGranted = onPermissionsGranted,
        onPermissionsDenied = onPermissionsDenied
    )
}

@Composable
private fun RequestNotificationPolicyAccessPermission(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (notificationManager.isNotificationPolicyAccessGranted) onPermissionsGranted()
        else onPermissionsDenied()
    }

    LaunchedEffect(Unit) {
        if (notificationManager.isNotificationPolicyAccessGranted) onPermissionsGranted()
        else launcher.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }
}

@Preview(widthDp = 350, heightDp = 700)
@Composable
private fun Preview() {
    Content()
}