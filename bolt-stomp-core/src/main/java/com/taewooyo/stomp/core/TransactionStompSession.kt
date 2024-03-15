package com.taewooyo.stomp.core

import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.header.StompSendHeaders

internal class TransactionStompSession(
  private val session: StompSession,
  private val transactionId: String,
) : StompSession by session {

  override suspend fun send(headers: StompSendHeaders, body: FrameBody?): StompReceipt? {
    headers.transaction = headers.transaction ?: transactionId
    return session.send(headers, body)
  }

  override suspend fun ack(ackId: String, transactionId: String?) {
    session.ack(ackId, transactionId ?: this.transactionId)
  }

  override suspend fun nack(ackId: String, transactionId: String?) {
    session.nack(ackId, transactionId ?: this.transactionId)
  }
}