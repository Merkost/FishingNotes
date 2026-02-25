package com.mobileprism.fishing.utils

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json

inline fun <reified T : Any> serializableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
): NavType<T> {
    return if (isNullableAllowed) {
        @Suppress("UNCHECKED_CAST")
        (nullableSerializableType<T>(json) as NavType<T>)
    } else {
        nonNullSerializableType(json)
    }
}

inline fun <reified T : Any> nonNullSerializableType(
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(value))
    }
}

inline fun <reified T : Any> nullableSerializableType(
    json: Json = Json,
) = object : NavType<T?>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T? {
        return if (value == "null") null else json.decodeFromString(value)
    }

    override fun serializeAsValue(value: T?): String {
        return value?.let { Uri.encode(json.encodeToString(it)) } ?: "null"
    }

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putString(key, value?.let { json.encodeToString(it) })
    }
}
