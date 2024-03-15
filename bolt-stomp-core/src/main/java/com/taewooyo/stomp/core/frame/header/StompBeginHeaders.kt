package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.TRANSACTION
import com.taewooyo.stomp.core.frame.header.delegate.header

/**
 * required : transaction optional : none
 */
data class StompBeginHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val transaction: String by header()

  constructor(transaction: String) : this(headerOf(TRANSACTION to transaction))
}