package com.taewooyo.stomp.core.heartbeats

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class HeartBeatTolerance(
  val outgoingMargin: Duration = Duration.ZERO,
  val incomingMargin: Duration = 500.milliseconds
)