package com.taewooyo.stomp.core

import com.taewooyo.core.CloseCodes
import com.taewooyo.core.WebSocketConnection
import com.taewooyo.core.WebSocketException
import com.taewooyo.core.WebsocketFrame
import com.taewooyo.core.truncateToCloseFrameReasonLength
import com.taewooyo.stomp.core.env.StompConfig
import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.StompFrame
import com.taewooyo.stomp.core.frame.StompStatus
import com.taewooyo.stomp.core.frame.header.StompConnectHeaders
import com.taewooyo.stomp.core.frame.messageHandler.decodeToStompFrame
import com.taewooyo.stomp.core.frame.messageHandler.encodeToByteString
import com.taewooyo.stomp.core.frame.messageHandler.encodeToText
import com.taewooyo.stomp.core.heartbeats.isHeartBeat
import com.taewooyo.stomp.core.heartbeats.sendHeartBeat
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class StompSocket(
  private val webSocketConnection: WebSocketConnection,
  private val config: StompConfig,
) {

  val receivingEvent: Flow<StompStatus> = webSocketConnection.receivingFrames
    .catch {
      config.monitoring?.onWebSocketClientError(it)
      throw it
    }
    .map { decodeStomp(it) }
    .catch { close(cause = it) }

  private suspend fun decodeStomp(webSocketFrame: WebsocketFrame): StompStatus {
    config.monitoring?.onWebSocketFrameReceived(webSocketFrame)
    val status = webSocketFrame.decodeToStompStatus()
    val frame = status as? StompFrame ?: return status
    config.monitoring?.onFrameDecoded(webSocketFrame, frame)
    if (frame is StompFrame.Error) {
      throw StompErrorFrameReceived(frame)
    }
    return frame
  }

  suspend fun close(cause: Throwable? = null) {
    if (cause !is WebSocketException) {
      webSocketConnection.close(cause)
    }
  }

  suspend fun sendStompFrame(frame: StompFrame) {
    if (frame.body is FrameBody.Binary) {
      webSocketConnection.sendBinary(frame.encodeToByteString())
    } else {
      webSocketConnection.sendText(frame.encodeToText())
    }
    config.monitoring?.onStompFrameSent(frame)
  }

  suspend fun sendHeartBeat() {
    webSocketConnection.sendHeartBeat()
  }
}

suspend fun StompSocket.connectHandshake(
  headers: StompConnectHeaders,
  connectWithStompCommand: Boolean,
): StompFrame.Connected = coroutineScope {
  val futureConnectedFrame = async(start = CoroutineStart.UNDISPATCHED) {
    awaitConnectedFrame()
  }
  val connectFrame = if (connectWithStompCommand) {
    StompFrame.Stomp(headers)
  } else {
    StompFrame.Connect(headers)
  }
  sendStompFrame(connectFrame)
  futureConnectedFrame.await()
}

private suspend fun StompSocket.awaitConnectedFrame(): StompFrame.Connected {
  val stompEvent = receivingEvent.firstOrNull()
  check(stompEvent is StompFrame.Connected) { "Expected CONNECTED frame in response to CONNECT, got $stompEvent" }
  return stompEvent
}

private fun WebsocketFrame.decodeToStompStatus(): StompStatus {
  if (isHeartBeat()) return StompStatus.HeartBeat
  return when (this) {
    is WebsocketFrame.Text -> text.decodeToStompFrame()
    is WebsocketFrame.Binary -> bytes.decodeToStompFrame()
    is WebsocketFrame.Ping,
    is WebsocketFrame.Pong
    -> StompStatus.HeartBeat // we need to count this traffic
    is WebsocketFrame.ConnectionClose -> throw Exception("the WebSocket was closed while subscriptions were still active. Code: $code Reason: $reason")
  }
}

private suspend fun WebSocketConnection.close(cause: Throwable?) {
  close(
    code = closeCodeFor(cause),
    reason = cause?.message?.truncateToCloseFrameReasonLength()
  )
}

private fun closeCodeFor(cause: Throwable?): Int = when (cause) {
  null -> CloseCodes.NORMAL_CLOSURE.code
  is MissingHeartBeatException -> 3002
  else -> 3001
}