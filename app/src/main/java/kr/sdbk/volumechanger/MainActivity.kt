package kr.sdbk.volumechanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toData
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toEntity
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.feature.list.ListView
import kr.sdbk.volumechanger.feature.map.MapView
import kr.sdbk.volumechanger.feature.splash.SplashView
import kr.sdbk.volumechanger.ui.theme.VolumeChangerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolumeChangerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VolumeChangerApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun VolumeChangerApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Splash,
        modifier = modifier
    ) {
        composable<Splash> {
            SplashView(
                navigateToList = {
                    navController.navigate(List) {
                        popUpTo(Splash) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<List> {
            ListView(
                navigateToMap = {
                    val data = it?.run { Json.encodeToString(toEntity()) }
                    navController.navigate(Map(data))
                }
            )
        }

        composable<Map> { backstackEntry ->
            val route: Map = backstackEntry.toRoute()
            val data: LocationEntity? = route.location?.run { Json.decodeFromString(this) }
            MapView(data?.toData())
        }
    }
}

@Serializable
object Splash

@Serializable
object List

@Serializable
data class Map(val location: String?)