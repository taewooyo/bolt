package com.taewooyo.okhttp

import com.taewooyo.core.WebSocketClient
import com.taewooyo.core.WebSocketConnection
import com.taewooyo.core.WebSocketConnectionException
import com.taewooyo.core.WebSocketListenerStreamAdapter
import com.taewooyo.core.WebsocketFrame
import com.taewooyo.core.extensions.readByteString
import com.taewooyo.core.extensions.unsafeBackingByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.net.SocketException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OkHttpWebSocketClient(
  private val client: OkHttpClient = OkHttpClient(),
) : WebSocketClient {

  override suspend fun connect(url: String, headers: Map<String, String>): WebSocketConnection {
    val request = Request.Builder()
      .url(url)
      .headers(headers.toHeaders())
      .build()

    return suspendCancellableCoroutine { continuation ->
      val okHttpListenerAdapter =
        OkHttpListenerAdapter(continuation, url, WebSocketListenerStreamAdapter())
      val ws = client.newWebSocket(request, okHttpListenerAdapter)
      continuation.invokeOnCancellation {
        ws.cancel()
      }
    }
  }
}

private class OkHttpListenerAdapter(
  connectionContinuation: Continuation<WebSocketConnection>,
  private val connectionUrl: String,
  private val channelListener: WebSocketListenerStreamAdapter,
) : WebSocketListener() {

  private var connectionContinuation: Continuation<WebSocketConnection>? = connectionContinuation

  @Volatile
  private var isConnecting = true

  private inline fun completeConnection(resume: Continuation<WebSocketConnection>.() -> Unit) {
    val cont = connectionContinuation ?: error(CONNECTION_ERROR_MESSAGE)
    connectionContinuation = null
    isConnecting = false
    cont.resume()
  }

  override fun onOpen(webSocket: WebSocket, response: Response) {
    val okHttpConnection = OkHttpConnectionAdapter(webSocket, channelListener.receivingFrames)
    completeConnection { resume(okHttpConnection) }
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    runBlocking { channelListener.onBinaryMessage(bytes.asByteBuffer().readByteString()) }
  }

  override fun onMessage(webSocket: WebSocket, text: String) {
    runBlocking { channelListener.onTextMessage(text) }
  }

  override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    runBlocking { runBlocking { channelListener.onClose(code, reason) } }
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    if (isConnecting) {
      val responseBody = try {
        response?.body?.string()?.takeIf { it.isNotBlank() }
      } catch (e: SocketException) {
        t.addSuppressed(e)
        null
      }
      val exception = WebSocketConnectionException(
        url = connectionUrl,
        httpStatusCode = response?.code,
        additionalInformation = responseBody,
        cause = t,
      )
      completeConnection {
        resumeWithException(exception)
      }
    } else {
      channelListener.onError(t)
    }
  }

  companion object {
    private const val CHANNEL_ERROR_MESSAGE = "WEBSOCKET ERROR"
    private const val CONNECTION_ERROR_MESSAGE = "OkHttp connection continuation already consumed"
  }
}

private class OkHttpConnectionAdapter(
  private val webSocket: WebSocket,
  override val receivingFrames: Flow<WebsocketFrame>
) : WebSocketConnection {

  override val url: String
    get() = webSocket.request().url.toString()

  override val canSend: Boolean
    get() = true

  override suspend fun sendText(text: String) {
    webSocket.send(text)
  }

  override suspend fun sendBinary(bytes: kotlinx.io.bytestring.ByteString) {
    webSocket.send(bytes.unsafeBackingByteArray().toByteString())
  }

  override suspend fun close(code: Int, reason: String?) {
    webSocket.close(code, reason)
  }
}