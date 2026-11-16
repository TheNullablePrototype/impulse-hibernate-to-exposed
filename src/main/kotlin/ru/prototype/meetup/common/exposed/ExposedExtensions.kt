package ru.prototype.meetup.common.exposed

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Table.create(createSchemaIfMissing: Boolean = true) {
    transaction {
        if (createSchemaIfMissing)
            tableName.substringBefore(".", missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
                ?.let { SchemaUtils.createSchema(Schema(it)) }
        SchemaUtils.create(this@create)
    }
}

fun Table.truncate(restartIdentity: Boolean = true, cascade: Boolean = false) {
    transaction {
        exec(buildString {
            append("TRUNCATE TABLE ${nameInDatabaseCase()}")
            if (restartIdentity) append(" RESTART IDENTITY")
            if (cascade) append(" CASCADE")
        })
    }
}

fun Table.selectFetch(columns: List<Expression<*>>, spec: Op<Boolean>? = null, fetchSize: Int = 1000) =
    this.select(columns)
        .where { spec ?: Op.TRUE }
        .fetchSize(fetchSize)
        .asSequence()