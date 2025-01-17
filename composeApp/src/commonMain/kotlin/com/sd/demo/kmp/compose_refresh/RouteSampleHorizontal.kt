package com.sd.demo.kmp.compose_refresh

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
import com.sd.lib.kmp.compose_refresh.FRefreshContainer
import com.sd.lib.kmp.compose_refresh.rememberRefreshStateEnd
import com.sd.lib.kmp.compose_refresh.rememberRefreshStateStart

@Composable
fun RouteSampleHorizontal(
  vm: PageViewModel = viewModel { PageViewModel() },
  onClickBack: () -> Unit,
) {
  val uiState by vm.uiState.collectAsState()

  // start
  val startRefreshState = rememberRefreshStateStart(uiState.isRefreshing) { vm.refresh(10) }

  // end
  val endRefreshState = rememberRefreshStateEnd(uiState.isLoadingMore) { vm.loadMore() }

  RouteScaffold(
    title = "SampleHorizontal",
    onClickBack = onClickBack,
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
        // start
        .nestedScroll(startRefreshState.nestedScrollConnection)
        // end
        .nestedScroll(endRefreshState.nestedScrollConnection),
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
  }

  LaunchedEffect(vm) {
    vm.refresh(10)
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