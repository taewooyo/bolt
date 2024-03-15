package com.taewooyo.stomp.core.frame

/**
 * https://blog.paimon.studio/54
 */
enum class StompCommand(
  internal val text: String,
  internal val supportsEscapes: Boolean = true
) {
  /**
   * required : accept-version, host
   * optional : login, passcode, heart-beat
   */
  STOMP("STOMP"),
  CONNECT("CONNECT", false),

  /**
   * required : version
   * optional : session, server, heart-beat
   */
  CONNECTED("CONNECTED", false),

  /**
   * required : destination
   * optional : transaction
   */
  SEND("SEND"),

  /**
   * required : destination, id
   * optional : ack
   */
  SUBSCRIBE("SUBSCRIBE"),

  /**
   * required : id
   * optional : none
   */
  UNSUBSCRIBE("UNSUBSCRIBE"),

  /**
   * required : id
   * optional : transaction
   */
  ACK("ACK"),
  NACK("NACK"),

  /**
   * required : transaction
   * optional : none
   */
  BEGIN("BEGIN"),
  COMMIT("COMMIT"),
  ABORT("ABORT"),

  /**
   * required : none
   * optional : receipt
   */
  DISCONNECT("DISCONNECT"),

  /**
   * required : destination, message-id, subscription
   * optional : ack
   */
  MESSAGE("MESSAGE"),

  /**
   * required : receipt-id
   * optional : none
   */
  RECEIPT("RECEIPT"),

  /**
   * required : none
   * optional : message
   */
  ERROR("ERROR");

  companion object {
    fun of(value: String): StompCommand {
      return valueOf(value)
    }
  }
}