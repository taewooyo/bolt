package com.taewooyo.stomp.core.heartbeats

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlin.time.Duration

internal class HeartBeater(
  private val heartBeat: HeartBeat,
  tolerance: HeartBeatTolerance,
  sendHeartBeat: suspend () -> Unit,
  onMissingHeartBeat: suspend () -> Unit,
) {
  private val outgoingTicker =
    Ticker(heartBeat.minSendPeriod - tolerance.outgoingMargin, sendHeartBeat)
  private val incomingTicker =
    Ticker(heartBeat.expectedPeriod + tolerance.incomingMargin, onMissingHeartBeat)

  fun startIn(scope: CoroutineScope): Job = scope.launch(CoroutineName("stomp-heart-beat")) {
    if (heartBeat.minSendPeriod > Duration.ZERO) {
      outgoingTicker.startIn(this + CoroutineName("stomp-heart-beat-outgoing"))
    }
    if (heartBeat.expectedPeriod > Duration.ZERO) {
      incomingTicker.startIn(this + CoroutineName("stomp-heart-beat-incoming"))
    }
  }

  fun notifyMsgSent() {
    outgoingTicker.reset()
  }

  fun notifyMsgReceived() {
    incomingTicker.reset()
  }
}

private class Ticker(
  val period: Duration,
  val onTick: suspend () -> Unit,
) {
  private val resetEvents = Channel<Unit>()

  @OptIn(ExperimentalCoroutinesApi::class) // for onTimeout
  fun startIn(scope: CoroutineScope): Job = scope.launch {
    while (isActive) {
      select<Unit> {
        resetEvents.onReceive { }
        onTimeout(period, onTick)
      }
    }
  }

  fun reset() {
    resetEvents.trySend(Unit)
  }
}