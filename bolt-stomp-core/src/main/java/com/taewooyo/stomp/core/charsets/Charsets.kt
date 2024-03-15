package com.taewooyo.stomp.core.charsets

import com.taewooyo.core.extensions.asByteString
import com.taewooyo.core.extensions.unsafeBackingByteArray
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import java.nio.charset.Charset

@OptIn(UnsafeByteStringApi::class)
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal fun String.encodeToByteString(charset: Charset): ByteString =
  (this as java.lang.String).getBytes(charset).asByteString()

@OptIn(UnsafeByteStringApi::class)
internal fun ByteString.decodeToString(charset: Charset): String =
  String(unsafeBackingByteArray(), charset)

internal fun String.toCharset(): Charset = Charset.forName(this)

internal fun extractCharset(mimeTypeText: String): Charset? = mimeTypeText.splitToSequence(';')
  .drop(1)
  .map { it.substringAfter("charset=", "") }
  .firstOrNull { it.isNotEmpty() }
  ?.toCharset()