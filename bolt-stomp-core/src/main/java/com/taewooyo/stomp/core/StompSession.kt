package com.taewooyo.stomp.core

import com.benasher44.uuid.uuid4
import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.StompFrame
import com.taewooyo.stomp.core.frame.header.StompSendHeaders
import com.taewooyo.stomp.core.frame.header.StompSubscribeHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.bytestring.ByteString

interface StompSession {
  suspend fun send(headers: StompSendHeaders, body: FrameBody?): StompReceipt?

  suspend fun subscribe(headers: StompSubscribeHeaders): Flow<StompFrame.Message>

  suspend fun ack(ackId: String, transactionId: String? = null)

  suspend fun nack(ackId: String, transactionId: String? = null)

  suspend fun begin(transactionId: String)

  suspend fun commit(transactionId: String)

  suspend fun abort(transactionId: String)

  suspend fun disconnect()
}

data class StompReceipt(val id: String)

suspend fun StompSession.sendBinary(destination: String, body: ByteString?): StompReceipt? =
  send(StompSendHeaders(destination), body?.let { FrameBody.Binary(it) })

suspend fun StompSession.sendText(destination: String, body: String?): StompReceipt? =
  send(StompSendHeaders(destination), body?.let { FrameBody.Text(it) })

suspend fun StompSession.sendEmptyMsg(destination: String): StompReceipt? =
  send(StompSendHeaders(destination), null)

suspend fun StompSession.subscribe(destination: String): Flow<StompFrame.Message> =
  subscribe(StompSubscribeHeaders(destination))

suspend fun StompSession.subscribeText(destination: String): Flow<String> =
  subscribe(destination).map { it.bodyAsText }

suspend fun StompSession.subscribeBinary(destination: String): Flow<ByteString> =
  subscribe(destination).map { it.body?.bytes ?: ByteString() }

suspend fun <T> StompSession.withTransaction(block: suspend StompSession.(transactionId: String) -> T): T {
  val transactionId = uuid4().toString()
  begin(transactionId)
  try {
    val result = TransactionStompSession(this, transactionId).block(transactionId)
    commit(transactionId)
    return result
  } catch (e: Exception) {
    try {
      abort(transactionId)
    } catch (abortException: Exception) {
      e.addSuppressed(abortException)
    }
    throw e
  }
}

suspend inline fun <S : StompSession, R> S.use(block: (S) -> R): R {
  try {
    return block(this)
  } finally {
    disconnect()
  }
}