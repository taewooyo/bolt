package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ID
import com.taewooyo.stomp.core.frame.header.delegate.header

/**
 * required : id optional : none
 */
data class StompUnsubscribeHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val id: String by header()

  constructor(id: String) : this(headerOf(ID to id))
}