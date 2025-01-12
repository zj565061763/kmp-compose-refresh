package com.sd.kmp.demo.compose_refresh.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RouteHome(
  modifier: Modifier = Modifier,
  onClickSampleVertical: () -> Unit,
  onClickSampleHorizontal: () -> Unit,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Button(onClick = onClickSampleVertical) {
      Text(text = "SampleVertical")
    }

    Button(onClick = onClickSampleHorizontal) {
      Text(text = "SampleHorizontal")
    }
  }
}