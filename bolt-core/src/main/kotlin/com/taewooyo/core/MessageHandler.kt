package com.taewooyo.core

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import kotlinx.io.readString
import kotlinx.io.write
import kotlinx.io.writeString

internal abstract class MessageHandler<T>(
  private val onMessageComplete: suspend (T) -> Unit,
) {
  private val buffer = Buffer()

  suspend fun processMessage(frameData: T, isLast: Boolean = true) {
    if (buffer.exhausted() && isLast) {
      onMessageComplete(frameData)
    } else {
      buffer.writePartialMessage(frameData = frameData)
      if (isLast) {
        val fullMsg = buffer.readCompleteMessage()
        onMessageComplete(fullMsg)
      }
    }
  }

  suspend fun processMessage(isLast: Boolean = true, writeData: Sink.() -> Unit) {
    buffer.writeData()
    if (isLast) {
      onMessageComplete(buffer.readCompleteMessage())
    }
  }

  abstract fun Sink.writePartialMessage(frameData: T)
  abstract fun Source.readCompleteMessage(): T

  fun close() {
    buffer.close()
  }
}

internal class TextMessageHandler(
  onMessageComplete: suspend (String) -> Unit,
) : MessageHandler<String>(onMessageComplete) {

  override fun Sink.writePartialMessage(frameData: String) = writeString(frameData)

  override fun Source.readCompleteMessage(): String = readString()
}

internal class BinaryMessageHandler(
  onMessageComplete: suspend (ByteString) -> Unit,
) : MessageHandler<ByteString>(onMessageComplete) {

  override fun Sink.writePartialMessage(frameData: ByteString) = write(frameData)

  override fun Source.readCompleteMessage(): ByteString = readByteString()
}