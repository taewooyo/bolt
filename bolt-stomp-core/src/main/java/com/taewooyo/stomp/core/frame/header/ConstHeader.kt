package com.taewooyo.stomp.core.frame.header

object ConstHeader {
  /**
   * stomp protocol version
   */
  const val ACCEPT_VERSION = "accept-version"

  /**
   * How to confirm receipt of a message(auto, client, client-individual)
   */
  const val ACK = "ack"

  /**
   * message content byte length
   */
  const val CONTENT_LENGTH = "content-length"

  /**
   * message content mime type
   */
  const val CONTENT_TYPE = "content-type"

  /**
   * the destination to which the message should be sent.
   */
  const val DESTINATION = "destination"

  /**
   * Indicates the heartbeat time between the client and server.
   *
   * For example, 10000,10000 means to send a heartbeat every 10 seconds.
   */
  const val HEART_BEAT = "heart-beat"

  /**
   * the host name of the server you want to connect to.
   *
   * It is generally similar to the Host header in HTTP.
   */
  const val HOST = "host"

  /**
   * Subscription identifier, used when subscribing to messages.
   */
  const val ID = "id"

  /**
   * The username you use to connect to the server.
   */
  const val LOGIN = "login"

  /**
   * The body of a message sent as an error message or for other purposes.
   */
  const val MESSAGE = "message"

  /**
   * Message identifier, used to distinguish messages.
   */
  const val MESSAGE_ID = "message-id"

  /**
   * This is the password you use when connecting to the server.
   */
  const val PASSCODE = "passcode"

  /**
   * This is an identifier used when a client requests a receipt after sending a message.
   *
   */
  const val RECEIPT = "receipt"

  /**
   * This is the value of the receipt header used when the server sends a receipt.
   */
  const val RECEIPT_ID = "receipt-id"

  /**
   * Name or version information that the server uses to identify itself to clients.
   */
  const val SERVER = "server"

  /**
   * A unique identifier provided by the server to identify the connection session.
   */
  const val SESSION = "session"

  /**
   * Identifier of the subscription to which the message was published.
   */
  const val SUBSCRIPTION = "subscription"

  /**
   * Identifier used when starting, committing, or rolling back a transaction.
   */
  const val TRANSACTION = "transaction"

  /**
   * This is the version of the STOMP protocol that the server notifies the client when the connection is successful.
   */
  const val VERSION = "version"
}