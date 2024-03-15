package com.taewooyo.stomp.core.frame.messageHandler

import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.StompFrame
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import kotlinx.io.write
import kotlinx.io.writeString

internal fun StompFrame.encodeToByteString(): ByteString {
  val buffer = Buffer()
  buffer.writeStompFrame(this)
  return buffer.readByteString()
}

private fun Sink.writeStompFrame(stompFrame: StompFrame) {
  writeString(stompFrame.preambleText)
  stompFrame.body?.bytes?.let { write(it) }
  writeByte(0)
}

internal fun StompFrame.encodeToText(): String = "$preambleText${body.encodeToText()}\u0000"

private fun FrameBody?.encodeToText(): String = when (this) {
  null -> ""
  is FrameBody.Binary -> throw IllegalArgumentException("Cannot encode text frame with binary body")
  is FrameBody.Text -> text
}

private val StompFrame.preambleText: String
  get() = buildString {
    append(command.text)
    append('\n')
    headers.forEach { (name, value) ->
      append(maybeEscape(name))
      append(':')
      append(maybeEscape(value))
      append('\n')
    }
    append('\n') // additional empty line to separate the preamble from the body
  }

private fun StompFrame.maybeEscape(s: String) =
  if (command.supportsEscapes) Escaper.escape(s) else s