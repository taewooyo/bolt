package com.taewooyo.core

enum class CloseCodes (val code: Int) {

    /**
     * Normal closure;
     *
     * the connection successfully completed whatever purpose for which it was created
     */
    NORMAL_CLOSURE(code = 1000),

    /**
     * The endpoint is going away, either because of a server failure or because the browser is navigating away from the page that opened the connection.
     */
    GOING_AWAY(code = 1001),

    /**
     * The endpoint is terminating the connection due to a protocol error.
     */
    PROTOCOL_ERROR(code = 1002),

    /**
     * The connection is being terminated because the endpoint received data of a type it cannot accept.
     *
     * example; a text-only endpoint received binary data.
     */
    UNSUPPORTED_DATA(code = 1003),

    /**
     * Indicates that no status code was provided even though one was expected.
     */
    NO_STATUS_Rcvd(code = 1005),

    /**
     * Indicates that a connection was closed abnormally(that is, with no close frame being sent) when a status code is expected.
     */
    ABNORMAL_CLOSURE(code = 1006),

    /**
     * The endpoint is terminating the connection because a message was received that contained inconsistent data.
     *
     * example; non-UTF-8 data within a text message.
     */
    INVALID_FRAME_PAYLOAD_DATA(code = 1007),

    /**
     * The endpoint is terminating the connection because it received a message that violates its policy.
     * This is a generic status code, used when codes 1003 and 1009 are not suitable.
     */
    POLICY_VIOLATION(code = 1008),

    /**
     * The endpoint is terminating the connection because a data frame was received that is too large.
     */
    MESSAGE_TOO_BIG(code = 1009),

    /**
     * The client is terminating the connection because it expected the server to negotiate one or more extension, but the server didn't.
     */
    MANDATORY_EXT(code = 1010),

    /**
     * The server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.
     */
    INTERNET_ERROR(code = 1011),

    /**
     * The server is terminating the connection because it is restarting.
     */
    SERVICE_RESTARTED(code = 1012),

    /**
     *The server is terminating the connection due to a temporary condition,
     *
     * example; it is overloaded and is casting off some of its clients.
     */
    TRY_AGAIN_LATER(code = 1013),

    /**
     * The server was acting as a gateway or proxy and received an invalid response from the upstream server.
     * This is similar to 502 HTTP Status Code.
     */
    BAD_GATEWAY(code = 1014),

    /**
     * Indicates that the connection was closed due to a failure to perform a TLS handshake.
     *
     * example; the server certificate can't be verified.
     */
    TLS_HANDSHAKE(code = 1015)
}