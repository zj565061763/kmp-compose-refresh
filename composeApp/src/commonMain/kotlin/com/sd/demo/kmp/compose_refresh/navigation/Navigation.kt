package com.sd.demo.kmp.compose_refresh.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
  @Serializable
  data object Home : AppRoute

  @Serializable
  data object SampleVertical : AppRoute

  @Serializable
  data object SampleHorizontal : AppRoute
}