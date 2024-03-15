package com.taewooyo.core

import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString

open class WebSocketException(message: String, cause: Throwable? = null) : Exception(message, cause)

open class WebSocketConnectionException(
  val url: String,
  val httpStatusCode: Int? = null,
  val additionalInformation: String? = null,
  message: String = "Can not connect to websocket" + when {
    httpStatusCode == null -> additionalInformation ?: "No additional details"
    additionalInformation == null -> "HTTP $httpStatusCode"
    else -> "HTTP $httpStatusCode: $additionalInformation"
  },
  cause: Throwable? = null
) : WebSocketException(message, cause)


const val CLOSE_REASON_MAX_LENGTH_BYTES = 123

fun String.truncateToCloseFrameReasonLength(): String = truncateUtf8BytesLengthTo(CLOSE_REASON_MAX_LENGTH_BYTES)

fun String.truncateUtf8BytesLengthTo(maxLength: Int): String {
  val utf8Bytes = encodeToByteString()
  if (utf8Bytes.size <= maxLength) {
    return this
  }
  return utf8Bytes.substring(0, maxLength).decodeToString().trimEnd { it == '\uFFFD' }
}