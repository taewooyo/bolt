package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.ID
import com.taewooyo.stomp.core.frame.header.ConstHeader.TRANSACTION
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader

/**
 * required : id optional : transaction
 */
data class StompNackHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val id: String by header()
  val transaction: String? by optionalHeader()

  constructor(id: String? = null, transaction: String? = null) : this(
    headerOf(
      ID to id,
      TRANSACTION to transaction,
    )
  )
}