package com.taewooyo.core

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString

class WebSocketListenerStreamAdapter(
  bufferSize: Int = Channel.BUFFERED,
  onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) {
  private val frames: Channel<WebsocketFrame> = Channel(bufferSize, onBufferOverflow)

  val receivingFrames: Flow<WebsocketFrame> = frames.receiveAsFlow()

  private val textMessageHandler = TextMessageHandler { frames.send(WebsocketFrame.Text(it)) }
  private val binaryMessageHandler = BinaryMessageHandler { frames.send(WebsocketFrame.Binary(it)) }

  suspend fun onBinaryMessage(bytes: ByteString, isLast: Boolean = true) {
    binaryMessageHandler.processMessage(bytes, isLast)
  }

  suspend fun onBinaryMessage(isLast: Boolean = true, writeData: Sink.() -> Unit) {
    binaryMessageHandler.processMessage(isLast, writeData)
  }

  suspend fun onTextMessage(text: String, isLast: Boolean = true) {
    textMessageHandler.processMessage(text, isLast)
  }

  suspend fun onTextMessage(isLast: Boolean = true, writeData: Sink.() -> Unit) {
    textMessageHandler.processMessage(isLast, writeData)
  }

  suspend fun onPing(bytes: ByteString) {
    frames.send(WebsocketFrame.Ping(bytes))
  }

  suspend fun onPong(bytes: ByteString) {
    frames.send(WebsocketFrame.Pong(bytes))
  }

  @OptIn(DelicateCoroutinesApi::class)
  suspend fun onClose(code: Int, reason: String?) {
    if (frames.isClosedForSend) {
      return
    }
    frames.send(WebsocketFrame.ConnectionClose(code, reason))
    frames.close()
    binaryMessageHandler.close()
  }

  fun onError(message: String) {
    frames.close(WebSocketException(message))
    binaryMessageHandler.close()
  }

  fun onError(error: Throwable?) {
    frames.close(WebSocketException(error?.message ?: "web socket error", cause = error))
    binaryMessageHandler.close()
  }
}