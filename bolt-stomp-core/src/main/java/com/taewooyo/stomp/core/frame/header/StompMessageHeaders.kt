package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ACK
import com.taewooyo.stomp.core.frame.header.ConstHeader.DESTINATION
import com.taewooyo.stomp.core.frame.header.ConstHeader.MESSAGE_ID
import com.taewooyo.stomp.core.frame.header.ConstHeader.SUBSCRIPTION
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader

/**
 * required : destination, message-id, subscription optional : ack
 */
data class StompMessageHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val destination: String by header()
  val messageId: String by header(MESSAGE_ID)
  val subscription: String by header()
  val ack: String? by optionalHeader()

  constructor(
    destination: String,
    messageId: String,
    subscription: String,
    ack: String? = null,
    headers: Map<String, String> = emptyMap(),
  ) : this(
    headerOf(
      DESTINATION to destination,
      MESSAGE_ID to messageId,
      SUBSCRIPTION to subscription,
      ACK to ack,
      headers = headers
    )
  )
}