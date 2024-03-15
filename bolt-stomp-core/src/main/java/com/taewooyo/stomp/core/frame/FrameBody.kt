package com.taewooyo.stomp.core.frame

import com.taewooyo.stomp.core.charsets.extractCharset
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import java.nio.charset.Charset


sealed class FrameBody {

  abstract val bytes: ByteString

  data class Text(val text: String) : FrameBody() {
    constructor(utf8Bytes: ByteString) : this(utf8Bytes.decodeToString())

    override val bytes by lazy { text.encodeToByteString() }
  }

  data class Binary(override val bytes: ByteString) : FrameBody() {

    internal fun decodeAsText(charset: Charset): String = bytes.decodeToString(charset)
  }

  fun asText(contentType: String?): String = when (this) {
    is FrameBody.Text -> text
    is FrameBody.Binary -> decodeAsText(inferCharset(contentType))
  }

  private fun inferCharset(contentTypeHeader: String?): Charset {
    if (contentTypeHeader == null) {
      throw UnsupportedOperationException("Binary frame without content-type header cannot be converted to text")
    }
    val charset = extractCharset(contentTypeHeader)
    return when {
      charset != null -> charset
      contentTypeHeader.startsWith("text/") -> Charsets.UTF_8
      else -> throw UnsupportedOperationException(
        "Binary frame with content-type '$contentTypeHeader' cannot be converted to text"
      )
    }
  }
}