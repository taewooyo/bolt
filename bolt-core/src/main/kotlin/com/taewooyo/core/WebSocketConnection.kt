package com.taewooyo.core

import kotlinx.coroutines.flow.Flow
import kotlinx.io.bytestring.ByteString

interface WebSocketConnection {

  val url: String

  val host: String
    get() = url.substringAfter("://")
      .substringBefore("/")
      .substringBefore(":")

  val canSend: Boolean
  val receivingFrames: Flow<WebsocketFrame>

  suspend fun sendText(text: String)

  suspend fun sendBinary(bytes: ByteString)

  suspend fun close(code: Int = CloseCodes.NORMAL_CLOSURE.code, reason: String? = null)
}