package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ACCEPT_VERSION
import com.taewooyo.stomp.core.frame.header.ConstHeader.CONTENT_LENGTH
import com.taewooyo.stomp.core.frame.header.ConstHeader.CONTENT_TYPE
import com.taewooyo.stomp.core.frame.header.ConstHeader.HEART_BEAT
import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.mutableOptionalHeader
import com.taewooyo.stomp.core.frame.header.delegate.mutableOptionalIntHeader
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader
import com.taewooyo.stomp.core.heartbeats.HeartBeat
import kotlin.time.Duration.Companion.milliseconds

interface StompHeaders : MutableMap<String, String> {
  var contentLength: Int?
  var contentType: String?
  var receipt: String?
}

internal data class DefaultStompHeaders(
  private val headers: MutableMap<String, String>,
) : StompHeaders, MutableMap<String, String> by headers {
  override var contentLength: Int? by mutableOptionalIntHeader(CONTENT_LENGTH)

  override var contentType: String? by mutableOptionalHeader(CONTENT_TYPE)

  override var receipt: String? by mutableOptionalHeader(RECEIPT)
}

internal fun headerOf(
  vararg pairs: Pair<String, String?>,
  headers: Map<String, String> = emptyMap()
): StompHeaders {
  val headersMap = mutableMapOf<String, String>()
  pairs.forEach { (key, value) -> value?.let { headersMap[key] = it } }
  headersMap.putAll(headers)
  return DefaultStompHeaders(headers = headersMap)
}

internal fun acceptVersionHeader() = header(ACCEPT_VERSION) { it.split(",") }

internal fun heartBeatHeader() = optionalHeader(HEART_BEAT) { it.toHeartBeat() }

private fun String.toHeartBeat(): HeartBeat {
  val (minSendPeriod, expectedReceivedPeriod) = split(",")
  return HeartBeat(
    minSendPeriod = minSendPeriod.toInt().milliseconds,
    expectedPeriod = expectedReceivedPeriod.toInt().milliseconds
  )
}