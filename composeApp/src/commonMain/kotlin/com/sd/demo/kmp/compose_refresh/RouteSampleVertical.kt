package com.sd.demo.kmp.compose_refresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.sd.lib.kmp.compose_refresh.rememberRefreshStateBottom
import com.sd.lib.kmp.compose_refresh.rememberRefreshStateTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSampleVertical(
  modifier: Modifier = Modifier,
  vm: PageViewModel = viewModel { PageViewModel() },
  onClickBack: () -> Unit,
) {
  val uiState by vm.uiState.collectAsState()

  // top
  val topRefreshState = rememberRefreshStateTop(uiState.isRefreshing) { vm.refresh(10) }

  // bottom
  val bottomRefreshState = rememberRefreshStateBottom(uiState.isLoadingMore) { vm.loadMore() }

  LaunchedEffect(vm) {
    vm.refresh(10)
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(text = "SampleVertical") },
        navigationIcon = {
          IconButton(onClick = onClickBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
      )
    },
  ) { padding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(padding)
        // top
        .nestedScroll(topRefreshState.nestedScrollConnection)
        // bottom
        .nestedScroll(bottomRefreshState.nestedScrollConnection),
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