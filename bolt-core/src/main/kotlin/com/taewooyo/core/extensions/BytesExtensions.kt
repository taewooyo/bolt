package com.taewooyo.core.extensions

import kotlinx.io.bytestring.*
import kotlinx.io.bytestring.unsafe.*
import java.nio.*

@OptIn(UnsafeByteStringApi::class)
fun ByteString.unsafeBackingByteArray(): ByteArray {
  lateinit var backingByteArray: ByteArray
  UnsafeByteStringOperations.withByteArrayUnsafe(this) {
    backingByteArray = it
  }
  return backingByteArray
}

@UnsafeByteStringApi
fun ByteArray.asByteString(): ByteString = UnsafeByteStringOperations.wrapUnsafe(this)

@OptIn(UnsafeByteStringApi::class)
fun ByteString.asReadOnlyByteBuffer(): ByteBuffer = ByteBuffer.wrap(unsafeBackingByteArray()).asReadOnlyBuffer()

@OptIn(UnsafeByteStringApi::class)
fun ByteBuffer.readByteString(): ByteString = readByteArray().asByteString()

private fun ByteBuffer.readByteArray(): ByteArray {
  val array = ByteArray(remaining())
  get(array)
  return array
}