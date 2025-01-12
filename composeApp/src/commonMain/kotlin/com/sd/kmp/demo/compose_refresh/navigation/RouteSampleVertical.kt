package com.sd.kmp.demo.compose_refresh.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sd.kmp.compose_refresh.FRefreshContainer
import com.sd.kmp.compose_refresh.rememberRefreshStateBottom
import com.sd.kmp.compose_refresh.rememberRefreshStateTop
import com.sd.kmp.demo.compose_refresh.ColumnView
import com.sd.kmp.demo.compose_refresh.PageViewModel
import com.sd.kmp.demo.compose_refresh.logMsg

@Composable
fun RouteSampleVertical(
  modifier: Modifier = Modifier,
  vm: PageViewModel = viewModel(),
) {
  val uiState by vm.uiState.collectAsState()

  // top
  val topRefreshState = rememberRefreshStateTop(uiState.isRefreshing) {
    vm.refresh(10)
  }

  // bottom
  val bottomRefreshState = rememberRefreshStateBottom(uiState.isLoadingMore) {
    vm.loadMore()
  }

  LaunchedEffect(vm) {
    vm.refresh(10)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      // top
      .nestedScroll(topRefreshState.nestedScrollConnection)
      // bottom
      .nestedScroll(bottomRefreshState.nestedScrollConnection)
  ) {
    ColumnView(uiState.list)

    // top
    FRefreshContainer(
      state = topRefreshState,
      modifier = Modifier.align(Alignment.TopCenter),
    )

    // bottom
    FRefreshContainer(
      state = bottomRefreshState,
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }

  LaunchedEffect(topRefreshState) {
    snapshotFlow { topRefreshState.currentInteraction }
      .collect {
        logMsg { "top currentInteraction:$it" }
      }
  }

  LaunchedEffect(bottomRefreshState) {
    snapshotFlow { bottomRefreshState.currentInteraction }
      .collect {
        logMsg { "bottom currentInteraction:$it" }
      }
  }
}