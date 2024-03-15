package com.taewooyo.stomp.core

import com.benasher44.uuid.uuid4
import com.taewooyo.core.WebSocketException
import com.taewooyo.stomp.core.env.StompConfig
import com.taewooyo.stomp.core.frame.FrameBody
import com.taewooyo.stomp.core.frame.StompCommand
import com.taewooyo.stomp.core.frame.StompFrame
import com.taewooyo.stomp.core.frame.StompStatus
import com.taewooyo.stomp.core.frame.header.ConstHeader
import com.taewooyo.stomp.core.frame.header.DefaultStompHeaders
import com.taewooyo.stomp.core.frame.header.StompAbortHeaders
import com.taewooyo.stomp.core.frame.header.StompAckHeaders
import com.taewooyo.stomp.core.frame.header.StompBeginHeaders
import com.taewooyo.stomp.core.frame.header.StompCommitHeaders
import com.taewooyo.stomp.core.frame.header.StompDisconnectHeaders
import com.taewooyo.stomp.core.frame.header.StompNackHeaders
import com.taewooyo.stomp.core.frame.header.StompSendHeaders
import com.taewooyo.stomp.core.frame.header.StompSubscribeHeaders
import com.taewooyo.stomp.core.frame.header.StompUnsubscribeHeaders
import com.taewooyo.stomp.core.heartbeats.HeartBeat
import com.taewooyo.stomp.core.heartbeats.HeartBeater
import com.taewooyo.stomp.core.heartbeats.NO_HEART_BEATS
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

internal class BaseStompSession(
  private val config: StompConfig,
  private val stompSocket: StompSocket,
  heartBeat: HeartBeat,
  coroutineContext: CoroutineContext = EmptyCoroutineContext,
) : StompSession {

  private val scope = CoroutineScope(CoroutineName("stomp-session") + coroutineContext)
  private val sharedStompEvents = MutableSharedFlow<StompStatus>(extraBufferCapacity = 32)

  private val heartBeater = if (heartBeat != NO_HEART_BEATS) {
    HeartBeater(
      heartBeat = heartBeat,
      tolerance = config.heartBeatTolerance,
      sendHeartBeat = {
        try {
          stompSocket.sendHeartBeat()
        } catch (e: WebSocketException) {
          shutdown("STOMP session failed: heart beat couldn't be sent", cause = e)
        }
      },
      onMissingHeartBeat = {
        val cause = MissingHeartBeatException(heartBeat.expectedPeriod)
        sharedStompEvents.emit(StompStatus.Error(cause))
        stompSocket.close(cause)
      },
    )
  } else {
    null
  }

  private val heartBeaterJob = heartBeater?.startIn(scope)

  init {
    scope.launch {
      stompSocket.receivingEvent
        .onEach { heartBeater?.notifyMsgReceived() }
        .materializeErrorsAndCompletion()
        .collect {
          sharedStompEvents.emit(it)
        }
    }

    scope.launch {
      sharedStompEvents.collect {
        when (it) {
          is StompStatus.Close -> shutdown("STOMP session disconnected")
          is StompStatus.Error -> shutdown(
            "STOMP session cancelled due to upstream error",
            cause = it.cause
          )

          else -> Unit
        }
      }
    }
  }

  private suspend fun shutdown(message: String, cause: Throwable? = null) {
    heartBeaterJob?.cancel()
    awaitSubscriptionsCompletion()
    scope.cancel(message, cause = cause)
  }

  private suspend fun awaitSubscriptionsCompletion() {
    withTimeoutOrNull(config.subscriptionCompletionTimeout) {
      sharedStompEvents.subscriptionCount.takeWhile { it > 0 }.collect()
    }
  }

  private suspend fun startSubscription(headers: StompSubscribeHeaders): ReceiveChannel<StompFrame.Message> {
    val subscriptionStarted = CompletableDeferred<Unit>()

    val subscriptionChannel = sharedStompEvents
      .onSubscription {
        try {
          prepareHeadersAndSendFrame(StompFrame.Subscribe(headers))
          subscriptionStarted.complete(Unit)
        } catch (e: Exception) {
          subscriptionStarted.completeExceptionally(e)
        }
      }
      .dematerializeErrorsAndCompletion()
      .filterIsInstance<StompFrame.Message>()
      .filter { it.headers.subscription == headers.id }
      .produceIn(scope)

    subscriptionStarted.await()
    return subscriptionChannel
  }

  override suspend fun send(headers: StompSendHeaders, body: FrameBody?): StompReceipt? {
    return prepareHeadersAndSendFrame(StompFrame.Send(headers, body))
  }

  private suspend fun prepareHeadersAndSendFrame(frame: StompFrame): StompReceipt? {
    maybeSetContentLength(frame)
    maybeSetAutoReceipt(frame)
    val receiptId = frame.headers.receipt
    if (receiptId == null) {
      sendStompFrame(frame)
      return null
    }
    sendAndWaitForReceipt(receiptId, frame)
    return StompReceipt(receiptId)
  }

  private fun maybeSetContentLength(frame: StompFrame) {
    if (config.autoContentLength && frame.headers.contentLength == null) {
      frame.headers.contentLength = frame.body?.bytes?.size ?: 0
    }
  }

  private fun maybeSetAutoReceipt(frame: StompFrame) {
    if (config.autoReceipt && frame.headers.receipt == null) {
      frame.headers.receipt = uuid4().toString()
    }
  }

  private suspend fun sendAndWaitForReceipt(receiptId: String, frame: StompFrame) {
    withTimeoutOrNull(frame.receiptTimeout) {
      sharedStompEvents
        .onSubscription {
          sendStompFrame(frame)
        }
        .dematerializeErrorsAndCompletion()
        .filterIsInstance<StompFrame.Receipt>()
        .firstOrNull { it.headers.receiptId == receiptId }
        ?: throw SessionDisconnectedException("The STOMP frames flow completed unexpectedly while waiting for RECEIPT frame with id='$receiptId'")
    } ?: throw LostReceiptException(receiptId, frame.receiptTimeout, frame)
  }

  private val StompFrame.receiptTimeout: Duration
    get() = if (command == StompCommand.DISCONNECT) config.disconnectTimeout else config.receiptTimeout

  override suspend fun subscribe(headers: StompSubscribeHeaders): Flow<StompFrame.Message> {
    val headersWithId = headers.withId()

    return startSubscription(headersWithId)
      .consumeAsFlow()
      .onCompletion {
        when (it) {
          is CancellationException -> {
            if (scope.isActive) {
              unsubscribe(headersWithId.id)
            } else {
            }
          }
          else -> Unit
        }
      }
  }

  private suspend fun unsubscribe(subscriptionId: String) {
    sendStompFrame(StompFrame.Unsubscribe(StompUnsubscribeHeaders(id = subscriptionId)))
  }

  override suspend fun ack(ackId: String, transactionId: String?) {
    sendStompFrame(StompFrame.Ack(StompAckHeaders(ackId, transactionId)))
  }

  override suspend fun nack(ackId: String, transactionId: String?) {
    sendStompFrame(StompFrame.Nack(StompNackHeaders(ackId, transactionId)))
  }

  override suspend fun begin(transactionId: String) {
    sendStompFrame(StompFrame.Begin(StompBeginHeaders(transactionId)))
  }

  override suspend fun commit(transactionId: String) {
    sendStompFrame(StompFrame.Commit(StompCommitHeaders(transactionId)))
  }

  override suspend fun abort(transactionId: String) {
    sendStompFrame(StompFrame.Abort(StompAbortHeaders(transactionId)))
  }

  private suspend fun sendStompFrame(frame: StompFrame) {
    stompSocket.sendStompFrame(frame)
    heartBeater?.notifyMsgSent()
  }

  override suspend fun disconnect() {
    if (config.gracefulDisconnect) {
      sendDisconnectFrameAndWaitForReceipt()
    }
    stompSocket.close()
    sharedStompEvents.emit(StompStatus.Close)
  }

  private suspend fun sendDisconnectFrameAndWaitForReceipt() {
    try {
      val receiptId = uuid4().toString()
      val disconnectFrame = StompFrame.Disconnect(StompDisconnectHeaders(receiptId))
      sendAndWaitForReceipt(receiptId, disconnectFrame)
    } catch (e: LostReceiptException) {
    }
  }
}

private fun StompSubscribeHeaders.withId(): StompSubscribeHeaders {
  val existingId = get(ConstHeader.ID)
  if (existingId != null) {
    return this
  }
  val rawHeadersCopy = HashMap(this)
  rawHeadersCopy[ConstHeader.ID] = uuid4().toString()
  return StompSubscribeHeaders(DefaultStompHeaders(rawHeadersCopy))
}

private fun Flow<StompStatus>.materializeErrorsAndCompletion(): Flow<StompStatus> =
  catch { emit(StompStatus.Error(cause = it)) }
    .onCompletion { if (it == null) emit(StompStatus.Close) }

private fun Flow<StompStatus>.dematerializeErrorsAndCompletion(): Flow<StompStatus> =
  takeWhile { it !is StompStatus.Close }
    .onEach { if (it is StompStatus.Error) throw it.cause }