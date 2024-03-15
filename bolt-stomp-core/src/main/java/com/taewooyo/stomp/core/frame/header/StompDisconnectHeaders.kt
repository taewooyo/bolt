package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT

/**
 * required : none optional : receipt
 */
data class StompDisconnectHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {

  constructor(receipt: String? = null) : this(headerOf(RECEIPT to receipt))
}