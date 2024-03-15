package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ACK
import com.taewooyo.stomp.core.frame.header.ConstHeader.DESTINATION
import com.taewooyo.stomp.core.frame.header.ConstHeader.ID
import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader

/**
 * required : destination, id optional : ack
 */
data class StompSubscribeHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val destination: String by header()
  val id: String by header()
  val ack: AckMode by optionalHeader(default = AckMode.AUTO) { AckMode.of(it) }

  constructor(
    destination: String,
    id: String? = null,
    ack: AckMode = AckMode.AUTO,
    receipt: String? = null,
    headers: Map<String, String> = emptyMap(),
  ) : this(
    headerOf(
      DESTINATION to destination,
      ID to id,
      ACK to ack.value,
      RECEIPT to receipt,
      headers = headers,
    )
  )
}