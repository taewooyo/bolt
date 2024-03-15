package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.MESSAGE
import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT_ID
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader

/**
 * required : none optional : message
 */
data class StompErrorHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val message: String? by optionalHeader()
  val receiptId: String? by optionalHeader(RECEIPT_ID)

  constructor(
    message: String? = null,
    receiptId: String? = null,
    headers: Map<String, String> = emptyMap(),
  ) : this(
    headerOf(
      MESSAGE to message,
      RECEIPT_ID to receiptId,
      headers = headers,
    )
  )
}