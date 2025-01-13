package com.sd.kmp.compose_refresh.indicator

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sd.kmp.compose_refresh.FRefreshState
import com.sd.kmp.compose_refresh.RefreshDirection
import com.sd.kmp.compose_refresh.RefreshInteraction

/**
 * 默认的刷新指示器
 */
@Composable
fun DefaultRefreshIndicator(
  state: FRefreshState,
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
  contentColor: Color = contentColorFor(backgroundColor),
  strokeWidth: Dp = 2.dp,
  size: Dp = 40.dp,
  spinnerSize: Dp = size.times(0.5f),
  padding: PaddingValues = PaddingValues(5.dp),
) {
  val animScale = remember(state) { Animatable(1f) }

  DisposableEffect(state) {
    val callback: suspend () -> Unit = { animScale.animateTo(0f) }
    state.registerHideRefreshing(callback)
    onDispose { state.unregisterHideRefreshing(callback) }
  }

  LaunchedEffect(state) {
    snapshotFlow { state.currentInteraction }.collect {
      if (it == RefreshInteraction.None) {
        animScale.snapTo(1f)
      }
    }
  }

  val showRefreshing = state.currentInteraction.let {
    it == RefreshInteraction.Refreshing
      || it == RefreshInteraction.FlingToRefresh
  }

  WrapperBox(
    modifier = modifier.graphicsLayer {
      scaleX = animScale.value
      scaleY = animScale.value
    },
    size = size,
    padding = padding,
    backgroundColor = backgroundColor,
  ) {
    GoogleRefreshIndicator(
      modifier = Modifier.graphicsLayer {
        this.rotationZ = when (state.refreshDirection) {
          RefreshDirection.Top -> 0f
          RefreshDirection.Right -> 90f
          RefreshDirection.Bottom -> 180f
          RefreshDirection.Left -> 270f
        }
      },
      isRefreshing = showRefreshing,
      progress = { state.progress },
      contentColor = contentColor,
      spinnerSize = spinnerSize,
      strokeWidth = strokeWidth,
    )
  }
}

@Composable
private fun WrapperBox(
  modifier: Modifier = Modifier,
  size: Dp,
  padding: PaddingValues,
  backgroundColor: Color,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier.padding(padding),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(size)
        .background(backgroundColor, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      content()
    }
  }
}