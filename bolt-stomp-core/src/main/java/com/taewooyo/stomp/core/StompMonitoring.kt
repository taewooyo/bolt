package com.taewooyo.stomp.core

import com.taewooyo.core.WebsocketFrame
import com.taewooyo.stomp.core.frame.StompFrame

interface StompMonitoring {

  suspend fun onWebSocketFrameReceived(frame: WebsocketFrame)

  suspend fun onFrameDecoded(originalFrame: WebsocketFrame, decodedFrame: StompFrame)

  suspend fun onStompFrameSent(frame: StompFrame)

  suspend fun onWebSocketClosed(cause: Throwable?)

  suspend fun onWebSocketClientError(exception: Throwable)

  operator fun plus(monitoring: StompMonitoring): StompMonitoring =
    StompMonitoringImpl(listOf(this, monitoring))

}

class StompMonitoringImpl(
  private val monitoring: List<StompMonitoring>
) : StompMonitoring {

  override suspend fun onWebSocketFrameReceived(frame: WebsocketFrame) {
    monitoring.forEach { it.onWebSocketFrameReceived(frame) }
  }

  override suspend fun onFrameDecoded(originalFrame: WebsocketFrame, decodedFrame: StompFrame) {
    monitoring.forEach { it.onFrameDecoded(originalFrame, decodedFrame) }
  }

  override suspend fun onStompFrameSent(frame: StompFrame) {
    monitoring.forEach { it.onStompFrameSent(frame) }
  }

  override suspend fun onWebSocketClosed(cause: Throwable?) {
    monitoring.forEach { it.onWebSocketClosed(cause) }
  }

  override suspend fun onWebSocketClientError(exception: Throwable) {
    monitoring.forEach { it.onWebSocketClientError(exception) }
  }

  override operator fun plus(newMonitoring: StompMonitoring): StompMonitoring =
    StompMonitoringImpl(monitoring + newMonitoring)
}