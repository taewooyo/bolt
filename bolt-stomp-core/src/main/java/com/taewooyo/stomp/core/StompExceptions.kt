package com.taewooyo.stomp.core

import com.taewooyo.stomp.core.frame.StompFrame
import kotlin.time.Duration

class StompErrorFrameReceived(val frame: StompFrame.Error) : Exception(frame.message)

class LostReceiptException(
  val receiptId: String,
  val configuredTimeout: Duration,
  val frame: StompFrame,
) :
  Exception("No RECEIPT frame received for receiptId '$receiptId' (in ${frame.command} frame) within $configuredTimeout")

class MissingHeartBeatException(
  val expectedPeriod: Duration,
) : Exception("A server heart beat was missing (expecting data every $expectedPeriod at most)")

class WebSocketClosedUnexpectedly(
  val code: Int,
  val reason: String?,
) :
  Exception("the WebSocket was closed while subscriptions were still active. Code: $code Reason: $reason")

class SessionDisconnectedException(message: String) : Exception(message)

class ConnectionTimeout(url: String, timeout: Duration) :
  ConnectionException(url, "Timed out waiting for $timeout when connecting to $url")

open class WebSocketConnectionException(
  url: String,
  message: String = "Failed to connect at web socket level to $url",
  cause: Throwable? = null,
) : ConnectionException(url, message, cause)

class StompConnectionException(val host: String?, cause: Throwable? = null) :
  ConnectionException(
    host ?: "null",
    "Failed to connect at STOMP protocol level to host '$host'",
    cause
  )

open class ConnectionException(val url: String, message: String, cause: Throwable? = null) :
  Exception(message, cause)