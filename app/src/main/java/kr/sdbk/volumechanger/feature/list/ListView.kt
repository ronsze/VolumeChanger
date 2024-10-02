package kr.sdbk.volumechanger.feature.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
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
import com.google.android.gms.maps.model.LatLng
import kr.sdbk.volumechanger.R
import kr.sdbk.volumechanger.data.model.Location
import kr.sdbk.volumechanger.ui.composable.BaseText
import kr.sdbk.volumechanger.ui.composable.LoadingView
import kr.sdbk.volumechanger.util.Constants

@Composable
fun ListView(
    navigateToMap: (Location?) -> Unit,
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
    list: List<Location>,
    navigateToMap: (Location?) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            top = 15.dp,
            bottom = 20.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        items(list) {
            LocationItemContainer(
                content = {
                    LocationItem(item = it)
                },
                onClickItem = { navigateToMap(it) }
            )
        }

        item {
            LocationItemContainer(
                content = {
                    LocationBlankItem()
                },
                onClickItem = { navigateToMap(null) }
            )
        }
    }
}

@Composable
private fun LocationItemContainer(
    content: @Composable () -> Unit,
    onClickItem: () -> Unit
) {
    Card(
        border = BorderStroke((1.5).dp, Color.LightGray),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClickItem() }
    ) {
        content()
    }
}

@Composable
private fun LocationItem(
    item: Location
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BaseText(
            text = item.name,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = item.enabled,
            onCheckedChange = { item.enabled = !item.enabled },
            modifier = Modifier
        )
    }
}

@Composable
private fun LocationBlankItem() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            imageVector = Icons.Filled.Add,
            contentDescription = "",
            modifier = Modifier
                .size(55.dp)
                .align(Alignment.Center)
        )
    }
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

@Preview(widthDp = 300)
@Composable
private fun LocationItemPreview() {
    LocationItemContainer(
        content = {
            LocationItem(
                item = Location(
                    created = 0,
                    name = "회사",
                    location = LatLng(35.2, 110.3),
                    500,
                    0,
                    0,
                    true
                )
            )
        },
        onClickItem = {}
    )
}

@Preview(widthDp = 300)
@Composable
private fun LocationBlankItemPreview() {
    LocationItemContainer(
        content = {
            LocationBlankItem()
        },
        onClickItem = {}
    )
}

@Preview
@Composable
private fun ErrorViewPreview() {
    ErrorView(errorMessage = Constants.UNKNOWN_ERROR) {

    }
}