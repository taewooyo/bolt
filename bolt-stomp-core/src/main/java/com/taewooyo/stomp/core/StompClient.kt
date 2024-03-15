package com.taewooyo.stomp.core

import com.taewooyo.core.WebSocketClient
import com.taewooyo.core.WebSocketConnection
import com.taewooyo.core.WebSocketConnectionException
import com.taewooyo.stomp.core.env.StompConfig
import com.taewooyo.stomp.core.frame.header.StompConnectHeaders
import com.taewooyo.stomp.core.heartbeats.negotiated
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

class StompClient(
  private val webSocketClient: WebSocketClient,
  private val config: StompConfig = StompConfig()
) {

  suspend fun connect(
    url: String,
    login: String? = null,
    passcode: String? = null,
    host: String? = url.substringAfter("://")
      .substringBefore("/")
      .substringBefore(":"),
    stompConnectHeaders: Map<String, String> = emptyMap(),
    sessionCoroutineContext: CoroutineContext = EmptyCoroutineContext
  ): StompSession {
    val session = withTimeoutOrNull(config.connectionTimeout) {
      val webSocket = webSocketConnect(url)
      val connectHeaders = StompConnectHeaders(
        host = host,
        login = login,
        passcode = passcode,
        heartBeat = config.heartBeat,
        headers = stompConnectHeaders,
      )
      webSocket.stomp(config, connectHeaders, sessionCoroutineContext)
    }
    return session ?: throw ConnectionTimeout(url, config.connectionTimeout)
  }

  private suspend fun webSocketConnect(url: String): WebSocketConnection {
    try {
      return webSocketClient.connect(url)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      throw WebSocketConnectionException(url, cause = e)
    }
  }
}

//suspend fun WebSocketConnection.stomp(
//  config: StompConfig,
//  host: String? = this.host,
//  login: String? = null,
//  passcode: String? = null,
//  headers: Map<String, String> = emptyMap(),
//  sessionCoroutineContext: CoroutineContext = EmptyCoroutineContext,
//): StompSession {
//  val connectHeaders = StompConnectHeaders(
//    host = host,
//    login = login,
//    passcode = passcode,
//    heartBeat = config.heartBeat,
//    headers = headers,
//  )
//  return stomp(config, connectHeaders, sessionCoroutineContext)
//}

private suspend fun WebSocketConnection.stomp(
  config: StompConfig,
  headers: StompConnectHeaders,
  sessionCoroutineContext: CoroutineContext,
): StompSession {
  val stompSocket = StompSocket(this, config)
  try {
    val connectedFrame = withTimeoutOrNull(config.connectionTimeout) {
      stompSocket.connectHandshake(headers, config.connectWithStompCommand)
    } ?: throw ConnectionTimeout(headers.host ?: "null", config.connectionTimeout)

    val negotiatedHeartBeat = config.heartBeat.negotiated(connectedFrame.headers.heartBeat)
    val contextOverrides = config.defaultSessionCoroutineContext + sessionCoroutineContext
    return BaseStompSession(config, stompSocket, negotiatedHeartBeat, contextOverrides)
  } catch (e: CancellationException) {
    withContext(NonCancellable) {
      stompSocket.close(e)
    }
    throw e
  } catch (e: ConnectionTimeout) {
    stompSocket.close(e)
    throw e
  } catch (e: Exception) {
    throw StompConnectionException(headers.host + " " + e.message, cause = e)
  }
}