package kr.sdbk.volumechanger.feature.list

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sdbk.volumechanger.R
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.ui.composable.BaseText
import kr.sdbk.volumechanger.ui.composable.LoadingView
import kr.sdbk.volumechanger.util.Constants

@Composable
fun ListView(
    navigateToMap: (LocationEntity?) -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) viewModel.loadData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        ListViewModel.ListUiState.Loading -> {
            LoadingView()
        }
        is ListViewModel.ListUiState.Loaded -> {
            Content(
                list = (uiState as ListViewModel.ListUiState.Loaded).list,
                navigateToMap = navigateToMap
            )
        }
        is ListViewModel.ListUiState.Failed -> {
            ErrorView(
                errorMessage = (uiState as ListViewModel.ListUiState.Failed).message,
                onRetry = viewModel::loadData
            )
        }
    }
}

@Composable
private fun Content(
    list: List<LocationEntity>,
    navigateToMap: (LocationEntity?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(list) {
            LocationItem(
                item = it,
                onClickItem = { navigateToMap(it) }
            )
        }
    }
}

@Composable
private fun LocationItem(
    item: LocationEntity,
    onClickItem: () -> Unit
) {

}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BaseText(
                text = errorMessage,
                color = Color.DarkGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_loading),
                contentDescription = "",
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRetry() }
            )
        }
    }
}

@Preview
@Composable
private fun ErrorViewPreview() {
    ErrorView(errorMessage = Constants.UNKNOWN_ERROR) {

    }
}