package com.sd.kmp.demo.compose_refresh.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
  @Serializable
  data object Home : AppRoute

  @Serializable
  data object SampleVertical : AppRoute

  @Serializable
  data object SampleHorizontal : AppRoute
}