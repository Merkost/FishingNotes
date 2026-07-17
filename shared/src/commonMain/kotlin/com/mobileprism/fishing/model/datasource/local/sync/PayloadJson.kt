package com.mobileprism.fishing.model.datasource.local.sync

import com.mobileprism.fishing.domain.entity.common.Note
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.longOrNull

fun payloadToJsonObject(map: Map<String, Any?>): JsonObject =
    JsonObject(map.mapValues { (_, value) -> anyToJsonElement(value) })

private fun anyToJsonElement(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is JsonElement -> value
    is String -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    is Note -> Json.encodeToJsonElement(value)
    is List<*> -> JsonArray(value.map { anyToJsonElement(it) })
    is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to anyToJsonElement(v) })
    else -> throw IllegalArgumentException("Unsupported payload value type: ${value::class.simpleName}")
}

fun jsonObjectToPayload(jsonObject: JsonObject): Map<String, Any> =
    jsonObject.entries.mapNotNull { (key, element) ->
        jsonElementToAny(element)?.let { key to it }
    }.toMap()

private fun jsonElementToAny(element: JsonElement): Any? = when (element) {
    is JsonNull -> null
    is JsonPrimitive -> when {
        element.isString -> element.content
        element.booleanOrNull != null -> element.booleanOrNull
        element.longOrNull != null -> element.longOrNull
        element.doubleOrNull != null -> element.doubleOrNull
        else -> element.content
    }
    is JsonArray -> element.mapNotNull { jsonElementToAny(it) }
    is JsonObject -> element.entries.mapNotNull { (key, value) ->
        jsonElementToAny(value)?.let { key to it }
    }.toMap()
}
