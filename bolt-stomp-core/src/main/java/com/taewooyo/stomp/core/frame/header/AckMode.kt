package com.taewooyo.stomp.core.frame.header

enum class AckMode(val value: String) {
  AUTO("auto"),
  CLIENT("client"),
  CLIENT_INDIVIDUAL("client-individual");

  companion object {
    fun of(value: String): AckMode {
      return valueOf(value)
    }
  }
}