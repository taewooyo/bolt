package com.taewooyo.core

interface WebSocketClient {

    suspend fun connect(url: String, headers: Map<String, String> = emptyMap()): WebSocketConnection
}