package com.taewooyo.stomp.core.frame.header.delegate

import com.taewooyo.stomp.core.frame.header.StompHeaders
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun header(key: String? = null): HeaderDelegate<String> = header(key) { it }

internal inline fun <T> header(
  key: String? = null,
  crossinline transform: (String) -> T
): HeaderDelegate<T> =
  HeaderDelegate(key) { value, k ->
    value?.let(transform) ?: throw IllegalStateException("missing required header '$k'")
  }

internal fun optionalHeader(key: String? = null, default: String? = null): HeaderDelegate<String?> =
  optionalHeader(key, default) { it }

internal inline fun <T> optionalHeader(
  customKey: String? = null,
  crossinline transform: (String) -> T,
): HeaderDelegate<T?> = optionalHeader(customKey, null, transform)

internal fun mutableOptionalHeader(
  customKey: String? = null,
  default: String? = null
): MutableHeaderDelegate<String?> {
  return mutableOptionalHeader(customKey, default, { it }, { it })
}

internal fun mutableOptionalIntHeader(
  key: String? = null,
  default: Int? = null
): MutableHeaderDelegate<Int?> {
  return mutableOptionalHeader(key, default, { it.toInt() }, { it.toString() })
}

internal inline fun <T> optionalHeader(
  key: String? = null,
  default: T,
  crossinline transform: (String) -> T
): HeaderDelegate<T> = HeaderDelegate(key) { value, _ -> value?.let(transform) ?: default }

internal inline fun <T> mutableOptionalHeader(
  key: String? = null,
  default: T,
  crossinline getTransform: (String) -> T,
  noinline setTransform: (T) -> String?
): MutableHeaderDelegate<T> = MutableHeaderDelegate(
  name = key,
  getTransform = { value, _ -> value?.let(getTransform) ?: default },
  setTransform = setTransform
)


internal open class HeaderDelegate<T>(
  private val name: String? = null,
  private val transform: (String?, String) -> T
) : ReadOnlyProperty<StompHeaders, T> {
  override fun getValue(thisRef: StompHeaders, property: KProperty<*>): T {
    val headerName = name ?: property.name
    return transform(thisRef[headerName], headerName)
  }
}

internal class MutableHeaderDelegate<T>(
  private val name: String? = null,
  getTransform: (String?, String) -> T,
  private val setTransform: (T) -> String?
) : HeaderDelegate<T>(name, getTransform), ReadWriteProperty<StompHeaders, T> {
  override fun setValue(thisRef: StompHeaders, property: KProperty<*>, value: T) {
    val headerName = name ?: property.name
    setTransform(value)?.let { newValue ->
      thisRef[headerName] = newValue
    } ?: thisRef.remove(headerName)
  }
}