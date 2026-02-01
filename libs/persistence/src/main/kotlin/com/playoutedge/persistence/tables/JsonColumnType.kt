package com.playoutedge.persistence.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun Table.jsonbColumn(name: String): Column<JsonObject> = jsonb<JsonObject>(
    name = name,
    serialize = { json.encodeToString(JsonObject.serializer(), it) },
    deserialize = { json.decodeFromString(JsonObject.serializer(), it) }
)

fun Table.jsonbColumnNullable(name: String): Column<JsonObject?> = jsonb<JsonObject>(
    name = name,
    serialize = { json.encodeToString(JsonObject.serializer(), it) },
    deserialize = { json.decodeFromString(JsonObject.serializer(), it) }
).nullable()
