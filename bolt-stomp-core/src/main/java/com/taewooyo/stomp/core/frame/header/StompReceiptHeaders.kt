package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.RECEIPT_ID
import com.taewooyo.stomp.core.frame.header.delegate.header

/**
 * required : receipt-id optional : none
 */
data class StompReceiptHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val receiptId: String by header(RECEIPT_ID)

  constructor(receiptId: String) : this(headerOf(RECEIPT_ID to receiptId))
}