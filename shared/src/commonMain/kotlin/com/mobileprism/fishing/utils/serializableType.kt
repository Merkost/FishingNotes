package com.mobileprism.fishing.utils

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
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
    override fun get(savedState: SavedState, key: String): T? =
        savedState.read { getString(key) }.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(savedState: SavedState, key: String, value: T) {
        savedState.write { putString(key, json.encodeToString(value)) }
    }
}

inline fun <reified T : Any> nullableSerializableType(
    json: Json = Json,
) = object : NavType<T?>(isNullableAllowed = true) {
    override fun get(savedState: SavedState, key: String): T? {
        val value = savedState.read { getString(key) }
        return if (value == "null") null else json.decodeFromString(value)
    }

    override fun parseValue(value: String): T? {
        return if (value == "null") null else json.decodeFromString(value)
    }

    override fun serializeAsValue(value: T?): String {
        return value?.let { json.encodeToString(it) } ?: "null"
    }

    override fun put(savedState: SavedState, key: String, value: T?) {
        savedState.write { putString(key, value?.let { json.encodeToString(it) } ?: "null") }
    }
}
