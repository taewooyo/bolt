package com.taewooyo.stomp.core.frame.header

import com.taewooyo.stomp.core.frame.header.ConstHeader.HEART_BEAT
import com.taewooyo.stomp.core.frame.header.ConstHeader.SERVER
import com.taewooyo.stomp.core.frame.header.ConstHeader.SESSION
import com.taewooyo.stomp.core.frame.header.ConstHeader.VERSION
import com.taewooyo.stomp.core.frame.header.delegate.header
import com.taewooyo.stomp.core.frame.header.delegate.optionalHeader
import com.taewooyo.stomp.core.heartbeats.HeartBeat

/**
 * required : version optional : session, server, heart-beat
 */
data class StompConnectedHeaders(private val rawHeaders: StompHeaders) :
  StompHeaders by rawHeaders {
  val version: String by header()
  val session: String? by optionalHeader()
  val server: String? by optionalHeader()
  val heartBeat: HeartBeat? by heartBeatHeader()

  constructor(
    version: String = "1.2",
    session: String? = null,
    server: String? = null,
    heartBeat: HeartBeat? = null,
  ) : this(
    headerOf(
      VERSION to version,
      SESSION to session,
      SERVER to server,
      HEART_BEAT to heartBeat?.formatAsHeaderValue(),
    )
  )
}