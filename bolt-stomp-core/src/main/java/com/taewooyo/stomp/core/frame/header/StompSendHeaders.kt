package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.DESTINATION
import com.taewooyo.stomp.core.frame.header.ConstHeader.HEART_BEAT
import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT
import com.taewooyo.stomp.core.frame.header.ConstHeader.SERVER
import com.taewooyo.stomp.core.frame.header.ConstHeader.SESSION
import com.taewooyo.stomp.core.frame.header.ConstHeader.TRANSACTION
import com.taewooyo.stomp.core.frame.header.ConstHeader.VERSION
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.mutableOptionalHeader
import com.taewooyo.stomp.core.heartbeats.HeartBeat

/**
 * required : destination optional : transaction
 */
data class StompSendHeaders(private val rawHeaders: StompHeaders) : StompHeaders by rawHeaders {
  val destination: String by header()
  var transaction: String? by mutableOptionalHeader()

  constructor(
    destination: String,
    transaction: String? = null,
    receipt: String? = null,
    headers: Map<String, String> = emptyMap(),
  ) : this(
    headerOf(
      DESTINATION to destination,
      TRANSACTION to transaction,
      RECEIPT to receipt,
      headers = headers,
    )
  )
}