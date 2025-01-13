package com.sd.lib.kmp.compose_refresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

interface FRefreshState {
  /** 嵌套滚动对象，外部需要把此对象传给[Modifier.nestedScroll] */
  val nestedScrollConnection: NestedScrollConnection

  /** 偏移量除以[refreshThreshold] */
  val progress: Float

  /** 当前互动状态 */
  val currentInteraction: RefreshInteraction

  /** 互动状态 */
  val interactionState: RefreshInteractionState

  /** 刷新方向 */
  val refreshDirection: RefreshDirection

  /** 可以触发刷新的距离 */
  val refreshThreshold: Float

  /** 显示刷新状态 */
  suspend fun showRefresh()

  /** 隐藏刷新状态 */
  suspend fun hideRefresh()

  /** 注册隐藏刷新回调 */
  fun registerHideRefreshing(callback: suspend () -> Unit)

  /** 取消注册隐藏刷新回调 */
  fun unregisterHideRefreshing(callback: suspend () -> Unit)
}

enum class RefreshDirection {
  Top, Bottom, Left, Right,
}

enum class RefreshInteraction {
  /** 原始 */
  None,

  /** 拖动 */
  Drag,

  /** 正在滑向刷新状态的位置 */
  FlingToRefresh,

  /** 刷新中 */
  Refreshing,

  /** 滑向原始的位置 */
  FlingToNone,
}

data class RefreshInteractionState(
  /** 当前互动状态 */
  val current: RefreshInteraction = RefreshInteraction.None,

  /** 上一个互动状态 */
  val previous: RefreshInteraction = RefreshInteraction.None,
)

internal class RefreshStateImpl(
  override val refreshDirection: RefreshDirection,
) : FRefreshState {
  override val progress: Float get() = _progressState
  override val currentInteraction: RefreshInteraction get() = _interactionState.current
  override val interactionState: RefreshInteractionState get() = _interactionState
  override val refreshThreshold: Float get() = _refreshThresholdState

  private var _enabled = false
  private var _offset = 0f
  private val _animProgress = Animatable(0f, Float.VectorConverter)

  private var _progressState by mutableFloatStateOf(0f)
  private var _interactionState by mutableStateOf(RefreshInteractionState())
  private var _refreshThresholdState by mutableFloatStateOf(0f)

  private var _onRefresh: (() -> Unit)? = null
  private val _hideRefreshingCallbacks: MutableSet<suspend () -> Unit> = mutableSetOf()

  override suspend fun showRefresh() {
    withContext(Dispatchers.Main) {
      cancelResetJob()
      animateToRefresh()
      setRefreshInteraction(RefreshInteraction.Refreshing)
    }
  }

  override suspend fun hideRefresh() {
    withContext(Dispatchers.Main) {
      cancelResetJob()
      notifyHideRefreshing()
      animateToReset()
    }
  }

  override fun registerHideRefreshing(callback: suspend () -> Unit) {
    _hideRefreshingCallbacks.add(callback)
  }

  override fun unregisterHideRefreshing(callback: suspend () -> Unit) {
    _hideRefreshingCallbacks.remove(callback)
  }

  private suspend fun notifyHideRefreshing() {
    if (currentInteraction == RefreshInteraction.Refreshing) {
      _hideRefreshingCallbacks.toTypedArray().forEach {
        try {
          it.invoke()
        } catch (e: Throwable) {
          // Ignore
        }
      }
    }
  }

  internal fun setRefreshThreshold(threshold: Float) {
    _refreshThresholdState = threshold.coerceAtLeast(0f)
  }

  internal fun setEnabled(enabled: Boolean) {
    _enabled = enabled
  }

  internal fun setOnRefresh(onRefresh: () -> Unit) {
    _onRefresh = onRefresh
  }

  private val _directionHandler = DirectionHandler(
    refreshDirection = refreshDirection,
    onPreScroll = { onPreScroll(it) },
    onPostScroll = { onPostScroll(it) },
    onPreFling = { onPreFling(it) },
  )

  private fun onPostScroll(available: Float): Float? {
    if (currentInteraction == RefreshInteraction.None) {
      val threshold = getThreshold() ?: return null
      val newOffset = calculateNewOffset(available, threshold)
      if (newOffset != 0f) {
        setRefreshInteraction(RefreshInteraction.Drag)
        _offset = newOffset
        _progressState = (newOffset / threshold).absoluteValue
        return available
      }
    }
    return null
  }

  private fun onPreScroll(available: Float): Float? {
    if (currentInteraction == RefreshInteraction.Drag) {
      val threshold = getThreshold() ?: return null
      val newOffset = calculateNewOffset(available, threshold)

      _offset = newOffset
      _progressState = (newOffset / threshold).absoluteValue

      if (newOffset == 0f) {
        setRefreshInteraction(RefreshInteraction.None)
      }
      return available
    }
    return null
  }

  private suspend fun onPreFling(available: Float): Float? {
    if (currentInteraction == RefreshInteraction.Drag) {
      if (_progressState >= 1f) {
        animateToRefresh()
        _resetJob = currentCoroutineContext()[Job]
        _onRefresh?.invoke()
        delay(200)
        _resetJob = null
      }
      animateToReset()
      return available
    }
    return null
  }

  private suspend fun animateToRefresh() {
    if (_progressState != 1f) {
      setRefreshInteraction(RefreshInteraction.FlingToRefresh)
    }
    animateToProgress(1f)
  }

  private suspend fun animateToReset() {
    if (_progressState != 0f) {
      setRefreshInteraction(RefreshInteraction.FlingToNone)
    }
    animateToProgress(0f)
    setRefreshInteraction(RefreshInteraction.None)
  }

  private suspend fun animateToProgress(progress: Float) {
    _animProgress.snapTo(_progressState)
    _animProgress.animateTo(progress) { _progressState = value }
  }

  private fun getThreshold(): Float? {
    val threshold = _refreshThresholdState
    return if (threshold > 0) {
      threshold
    } else {
      _progressState = 0f
      setRefreshInteraction(RefreshInteraction.None)
      null
    }
  }

  private fun setRefreshInteraction(current: RefreshInteraction) {
    val state = _interactionState
    if (state.current == current) return

    if (current == RefreshInteraction.None) {
      _offset = 0f
    }

    _interactionState = state.copy(
      previous = state.current,
      current = current,
    )
  }

  private fun calculateNewOffset(available: Float, threshold: Float): Float {
    val transform = transformAvailable(available, threshold)
    return (_offset + transform).let { offset ->
      when (refreshDirection) {
        RefreshDirection.Top, RefreshDirection.Left -> offset.coerceAtLeast(0f)
        RefreshDirection.Bottom, RefreshDirection.Right -> offset.coerceAtMost(0f)
      }
    }
  }

  private fun transformAvailable(
    available: Float,
    threshold: Float,
  ): Float {
    require(threshold > 0)
    val maxDragDistance = (threshold * 3f).takeIf { it.isFinite() } ?: Float.MAX_VALUE
    val currentProgress = (_offset / maxDragDistance).absoluteValue.coerceIn(0f, 1f)
    val multiplier = (1f - currentProgress).coerceIn(0f, 0.6f)
    return available * multiplier
  }

  override val nestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(
      available: Offset,
      source: NestedScrollSource,
    ): Offset {
      return if (source == NestedScrollSource.UserInput && _enabled) {
        _directionHandler.handlePreScroll(available)
      } else {
        Offset.Zero
      }
    }

    override fun onPostScroll(
      consumed: Offset,
      available: Offset,
      source: NestedScrollSource,
    ): Offset {
      return if (source == NestedScrollSource.UserInput && _enabled) {
        _directionHandler.handlePostScroll(available)
      } else {
        Offset.Zero
      }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
      return _directionHandler.handlePreFling(available)
    }
  }

  private var _resetJob: Job? = null
  private fun cancelResetJob() {
    _resetJob?.cancel()
    _resetJob = null
  }
}