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
import kotlinx.serialization.Serializable
import kr.sdbk.volumechanger.feature.list.ListView
import kr.sdbk.volumechanger.feature.map.MapView
import kr.sdbk.volumechanger.feature.splash.SplashView
import kr.sdbk.volumechanger.ui.theme.VolumeChangerTheme

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
                navigateToList = { navController.navigate(List) }
            )
        }

        composable<List> {
            ListView()
        }

        composable<Map> {
            MapView()
        }
    }
}

@Serializable
data object Splash

@Serializable
data object List

@Serializable
data object Map