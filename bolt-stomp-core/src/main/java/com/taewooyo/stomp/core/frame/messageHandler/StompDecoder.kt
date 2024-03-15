package com.taewooyo.stomp.core.frame.messageHandler

import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.StompCommand
import com.taewooyo.stomp.core.frame.StompFrame
import com.taewooyo.stomp.core.frame.header.DefaultStompHeaders
import com.taewooyo.stomp.core.frame.header.StompHeaders
import kotlinx.io.*
import kotlinx.io.bytestring.*

private const val NULL_BYTE: Byte = 0

internal fun ByteString.decodeToStompFrame() = toSource().readStompFrame(isBinary = true)

internal fun String.decodeToStompFrame() = toSource().readStompFrame(isBinary = false)

private fun ByteString.toSource(): Source = Buffer().apply { write(this@toSource) }
private fun String.toSource(): Source = Buffer().apply { writeString(this@toSource) }

private fun Source.readStompFrame(isBinary: Boolean): StompFrame {
  try {
    val command = readStompCommand()
    val headers = readStompHeaders(command.supportsEscapes)
    val body = readBodyBytes(headers.contentLength)?.toFrameBody(isBinary)
    expectNullOctet()
    expectOnlyEOLs()
    return StompFrame.create(command, headers, body)
  } catch (e: Exception) {
    throw Exception("Failed to decode invalid STOMP frame", e.cause)
  }
}

private fun Source.readStompCommand(): StompCommand {
  val firstLine = readLine() ?: error("Missing command in STOMP frame")
  return StompCommand.of(firstLine)
}

private fun Source.readStompHeaders(shouldUnescapeHeaders: Boolean): StompHeaders =
  generateSequence { readLineStrict() }
    .takeWhile { it.isNotEmpty() } // empty line marks end of headers
    .parseLinesAsStompHeaders(shouldUnescapeHeaders)

private fun Sequence<String>.parseLinesAsStompHeaders(shouldUnescapeHeaders: Boolean): StompHeaders {
  val headersMap = mutableMapOf<String, String>()
  forEach { line ->
    val (rawKey, rawValue) = line.split(':', ignoreCase = false, limit = 2)
    val key = if (shouldUnescapeHeaders) Escaper.unescape(rawKey) else rawKey
    val value = if (shouldUnescapeHeaders) Escaper.unescape(rawValue) else rawValue

    if (!headersMap.containsKey(key)) {
      headersMap[key] = value
    }
  }
  return DefaultStompHeaders(headers = headersMap)
}

private fun Source.readBodyBytes(contentLength: Int?) = when (contentLength) {
  0 -> null
  else -> readByteString(contentLength ?: indexOf(NULL_BYTE).toInt())
}

private fun ByteString.toFrameBody(binary: Boolean) = when {
  isEmpty() -> null
  binary -> FrameBody.Binary(this)
  else -> FrameBody.Text(this)
}

private fun Source.expectNullOctet() {
  if (readByte() != NULL_BYTE) {
    error("Expected NULL byte at end of frame")
  }
}

private fun Source.expectOnlyEOLs() {
  if (!exhausted()) {
    val endText = readString()
    if (endText.any { it != '\n' && it != '\r' }) {
      error("Unexpected non-EOL characters after end-of-frame NULL character: $endText")
    }
  }
}