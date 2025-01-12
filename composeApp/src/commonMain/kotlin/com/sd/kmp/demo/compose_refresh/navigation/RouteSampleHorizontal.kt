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
import com.sd.kmp.compose_refresh.rememberRefreshStateEnd
import com.sd.kmp.compose_refresh.rememberRefreshStateStart
import com.sd.kmp.demo.compose_refresh.PageViewModel
import com.sd.kmp.demo.compose_refresh.RowView
import com.sd.kmp.demo.compose_refresh.logMsg

@Composable
fun RouteSampleHorizontal(
  modifier: Modifier = Modifier,
  vm: PageViewModel = viewModel(),
) {
  val uiState by vm.uiState.collectAsState()

  // start
  val startRefreshState = rememberRefreshStateStart(uiState.isRefreshing) {
    vm.refresh(10)
  }

  // end
  val endRefreshState = rememberRefreshStateEnd(uiState.isLoadingMore) {
    vm.loadMore()
  }

  LaunchedEffect(vm) {
    vm.refresh(10)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      // start
      .nestedScroll(startRefreshState.nestedScrollConnection)
      // end
      .nestedScroll(endRefreshState.nestedScrollConnection)
  ) {
    RowView(uiState.list)

    // start
    FRefreshContainer(
      state = startRefreshState,
      modifier = Modifier.align(Alignment.CenterStart),
    )

    // end
    FRefreshContainer(
      state = endRefreshState,
      modifier = Modifier.align(Alignment.CenterEnd),
    )
  }

  LaunchedEffect(startRefreshState) {
    snapshotFlow { startRefreshState.interactionState }
      .collect {
        logMsg { "start interactionState:$it" }
      }
  }

  LaunchedEffect(endRefreshState) {
    snapshotFlow { endRefreshState.interactionState }
      .collect {
        logMsg { "end interactionState:$it" }
      }
  }
}