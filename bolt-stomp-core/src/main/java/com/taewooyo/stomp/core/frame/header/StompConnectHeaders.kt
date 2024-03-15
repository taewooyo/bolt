package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ACCEPT_VERSION
import com.taewooyo.stomp.core.frame.header.ConstHeader.HEART_BEAT
import com.taewooyo.stomp.core.frame.header.ConstHeader.HOST
import com.taewooyo.stomp.core.frame.header.ConstHeader.LOGIN
import com.taewooyo.stomp.core.frame.header.ConstHeader.PASSCODE
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader
import com.taewooyo.stomp.core.heartbeats.HeartBeat

/**
 * required : accept-version, host optional : login, passcode, heart-beat
 */
data class StompConnectHeaders(private val rawHeaders: StompHeaders) : StompHeaders by rawHeaders {
  val host: String? by optionalHeader()
  val acceptVersion: List<String> by acceptVersionHeader()
  val login: String? by optionalHeader()
  val passcode: String? by optionalHeader()
  val heartBeat: HeartBeat? by heartBeatHeader()

  constructor(
    host: String?,
    acceptVersion: List<String> = listOf("1.2"),
    login: String? = null,
    passcode: String? = null,
    heartBeat: HeartBeat? = null,
    headers: Map<String, String> = emptyMap()
  ) : this(
    headerOf(
      HOST to host,
      ACCEPT_VERSION to acceptVersion.joinToString(","),
      LOGIN to login,
      PASSCODE to passcode,
      HEART_BEAT to heartBeat?.formatAsHeaderValue(),
      headers = headers
    )
  )
}