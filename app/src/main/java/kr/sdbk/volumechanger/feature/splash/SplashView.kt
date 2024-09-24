package kr.sdbk.volumechanger.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.sdbk.volumechanger.R
import kr.sdbk.volumechanger.base.ErrorAlertState
import kr.sdbk.volumechanger.ui.composable.ErrorAlert

@Composable
fun SplashView(
    navigateToList: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) viewModel.loadData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var errorAlertState by remember { mutableStateOf(ErrorAlertState()) }
    if (uiState is SplashViewModel.SplashUiState.Failed) errorAlertState = ErrorAlertState(true, uiState.message)
    
    LaunchedEffect(key1 = uiState) {
        if (uiState == SplashViewModel.SplashUiState.Loaded) navigateToList()
    }
    
    Content()

    if (errorAlertState.isVisible) {
        ErrorAlert(
            message = errorAlertState.message,
            onDismissRequest = { errorAlertState = ErrorAlertState() },
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

@Preview(widthDp = 350, heightDp = 700)
@Composable
private fun Preview() {
    Content()
}