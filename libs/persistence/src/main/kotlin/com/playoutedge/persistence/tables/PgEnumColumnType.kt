package com.playoutedge.persistence.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject

/**
 * Custom column type for PostgreSQL native ENUM types.
 * This properly handles type casting when comparing enum values in queries.
 */
class PgEnumColumnType<T : Enum<T>>(
    private val enumName: String,
    private val enumClass: Class<T>
) : ColumnType<T>() {

    override fun sqlType(): String = enumName

    @Suppress("UNCHECKED_CAST")
    override fun valueFromDB(value: Any): T {
        return when {
            enumClass.isInstance(value) -> value as T
            value is PGobject -> enumClass.enumConstants.first { it.name == value.value }
            value is String -> enumClass.enumConstants.first { it.name == value }
            else -> error("Unknown value type for enum: ${value::class}")
        }
    }

    override fun notNullValueToDB(value: T): Any {
        return PGobject().apply {
            type = enumName
            this.value = value.name
        }
    }

    override fun nonNullValueToString(value: T): String {
        return "'${value.name}'::$enumName"
    }

    override fun valueToString(value: T?): String {
        return value?.let { nonNullValueToString(it) } ?: super.valueToString(value)
    }
}

/**
 * Creates a column for a PostgreSQL native ENUM type.
 *
 * Usage:
 * ```
 * val status = pgEnum<JobStatus>("status", "job_status")
 * ```
 */
inline fun <reified T : Enum<T>> Table.pgEnum(name: String, enumTypeName: String): Column<T> =
    registerColumn(name, PgEnumColumnType(enumTypeName, T::class.java))
