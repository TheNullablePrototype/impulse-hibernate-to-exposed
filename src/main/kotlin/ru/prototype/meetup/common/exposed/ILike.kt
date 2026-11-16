@file:Suppress("unused")

package ru.prototype.meetup.common.exposed

import org.jetbrains.exposed.v1.core.*

/** ILIKE (PostgreSQL) - LIKE без учёта регистра. В Exposed его нет, но свой оператор - пара строк. */
class InsensitiveLikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.iLike(pattern: String): Op<Boolean> =
    InsensitiveLikeOp(this, stringParam(pattern))

infix fun <T : String?> ExpressionWithColumnType<T>.notILike(pattern: String): Op<Boolean> =
    not(this iLike pattern)

/**
 * Содержит [text] без учёта регистра. `%` и `_` из пользовательского ввода экранируются
 * ([LikePattern.ofLiteral]) и ищутся как обычные символы, а не как шаблон.
 */
infix fun <T : String?> ExpressionWithColumnType<T>.iContains(text: String): Op<Boolean> =
    this iLike "%${LikePattern.ofLiteral(text).pattern}%"