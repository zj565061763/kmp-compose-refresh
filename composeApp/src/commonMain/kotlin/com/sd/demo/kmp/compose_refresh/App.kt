package com.sd.demo.kmp.compose_refresh

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
  MaterialTheme {
    val navController = rememberNavController()
    NavHost(
      navController = navController,
      startDestination = AppRoute.Home,
    ) {
      composable<AppRoute.Home> {
        RouteHome(
          onClickSampleVertical = { navController.navigate(AppRoute.SampleVertical) },
          onClickSampleHorizontal = { navController.navigate(AppRoute.SampleHorizontal) },
        )
      }
      composable<AppRoute.SampleVertical> {
        RouteSampleVertical(onClickBack = { navController.popBackStack() })
      }
      composable<AppRoute.SampleHorizontal> {
        RouteSampleHorizontal(onClickBack = { navController.popBackStack() })
      }
    }
  }
}

expect fun logMsg(tag: String = "kmp-compose-refresh", block: () -> String)