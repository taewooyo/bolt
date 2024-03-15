package com.taewooyo.stomp.core.heartbeats

import com.taewooyo.core.WebSocketConnection
import com.taewooyo.core.WebsocketFrame
import kotlinx.io.bytestring.ByteString
import kotlin.time.Duration


data class HeartBeat(
  val minSendPeriod: Duration = Duration.ZERO,
  val expectedPeriod: Duration = Duration.ZERO
) {
  fun formatAsHeaderValue() =
    "${minSendPeriod.inWholeMilliseconds},${expectedPeriod.inWholeMilliseconds}"
}

internal fun WebsocketFrame.isHeartBeat(): Boolean = when (this) {
  is WebsocketFrame.Text -> text == "\n" || text == "\r\n"
  is WebsocketFrame.Binary -> bytes.isEOL()
  else -> false
}

private const val CR = '\r'.code.toByte()
private const val LF = '\n'.code.toByte()

private fun ByteString.isEOL() = when (size) {
  1 -> get(0) == LF
  2 -> get(0) == CR && get(1) == LF
  else -> false
}

internal suspend fun WebSocketConnection.sendHeartBeat() {
  sendText("\n")
}

internal val NO_HEART_BEATS = HeartBeat(Duration.ZERO, Duration.ZERO)

internal fun HeartBeat.negotiated(serverHeartBeats: HeartBeat?): HeartBeat = HeartBeat(
  minSendPeriod = computeNegotiatedPeriod(minSendPeriod, serverHeartBeats?.expectedPeriod),
  expectedPeriod = computeNegotiatedPeriod(expectedPeriod, serverHeartBeats?.minSendPeriod),
)

private fun computeNegotiatedPeriod(clientPeriod: Duration, serverPeriod: Duration?): Duration = when {
  serverPeriod == null || serverPeriod == Duration.ZERO || clientPeriod == Duration.ZERO -> Duration.ZERO
  else -> maxOf(clientPeriod, serverPeriod)
}