package com.taewooyo.stomp.core.env

import com.taewooyo.stomp.core.StompMonitoring
import com.taewooyo.stomp.core.heartbeats.HeartBeat
import com.taewooyo.stomp.core.heartbeats.HeartBeatTolerance
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class StompConfig {
  var autoReceipt: Boolean = false

  var autoContentLength: Boolean = true

  var connectWithStompCommand: Boolean = false

  var heartBeat: HeartBeat = HeartBeat()

  var heartBeatTolerance: HeartBeatTolerance = HeartBeatTolerance()

  var connectionTimeout: Duration = 60.seconds

  var receiptTimeout: Duration = 1.seconds

  var disconnectTimeout: Duration = 200.milliseconds

  var subscriptionCompletionTimeout: Duration = 1.seconds

  var gracefulDisconnect: Boolean = true

  var defaultSessionCoroutineContext: CoroutineContext = EmptyCoroutineContext

  var monitoring: StompMonitoring? = null
}