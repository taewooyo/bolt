package com.taewooyo.stomp.core.frame.messageHandler

internal object Escaper {

  private const val ESCAPE_CHAR = '\\'

  fun escape(text: String): String = if (text.isEmpty()) text else buildEscapedString(text)

  private fun buildEscapedString(str: String): String = buildString(str.length) {
    for (c in str) {
      when (c) {
        '\r' -> append("""\r""")
        '\n' -> append("""\n""")
        ':' -> append("""\c""")
        '\\' -> append("""\\""")
        else -> append(c)
      }
    }
  }

  fun unescape(text: String): String = if (text.isEmpty()) text else buildUnescapedString(text)

  private fun buildUnescapedString(escapedStr: String): String = buildString(escapedStr.length) {
    var escaping = false
    for (c in escapedStr) {
      if (escaping) {
        append(c.whenEscaped())
        escaping = false
      } else {
        when (c) {
          ESCAPE_CHAR -> escaping = true
          else -> append(c)
        }
      }
    }
    if (escaping) {
      throw IllegalArgumentException("Invalid dangling escape character at end of text $ESCAPE_CHAR")
    }
  }

  private fun Char.whenEscaped(): Char = when (this) {
    'r' -> '\r'
    'n' -> '\n'
    'c' -> ':'
    '\\' -> '\\'
    else -> throw IllegalArgumentException("Invalid header escape sequence $ESCAPE_CHAR$this")
  }
}
