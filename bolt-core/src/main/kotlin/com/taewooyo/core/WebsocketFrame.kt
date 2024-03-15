package com.taewooyo.core

import kotlinx.io.bytestring.ByteString

sealed class WebsocketFrame {

  /**
   * 0x1(Text): This means that the included data is UTF-8 text.
   */
  data class Text(val text: String) : WebsocketFrame()

  /**
   * 0x2(Binary): This means that the included data is binary data.
   */
  data class Binary(val bytes: ByteString) : WebsocketFrame()

  /**
   * 0x9(Ping): A frame used to check whether the connection is maintained. A frame automatically generated and sent by the server or browser.
   */
  data class Ping(val bytes: ByteString) : WebsocketFrame()

  /**
   * 0xA(Pong): A frame used to check whether the connection is maintained. A frame automatically generated and sent by the server or browser.
   */
  data class Pong(val bytes: ByteString) : WebsocketFrame()

  /**
   * 0x8(Close): This means starting a close handshake.
   */
  data class ConnectionClose(val code: Int, val reason: String?) : WebsocketFrame()
}