package ru.prototype.meetup.common.exposed

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import ru.prototype.meetup.common.UnsupportedSortException

/**
 * Аналог `findAll(spec, pageable)` из Spring Data: условие + страница + `Page<T>`.
 *
 * Как в Spring Data, сначала читаем страницу, а COUNT выполняем, только если без него total не вычислить
 * (страница полная или не первая) — см. [PageableExecutionUtils].
 *
 * [countQuery] - запрос для COUNT, если его можно сделать дешевле основного (например, без join).
 * [spec] применяется к обоим запросам, поэтому он не должен ссылаться на таблицы, которых нет в [countQuery].
 */
fun <T : Any> Query.toPage(
    pageable: Pageable,
    sortColumns: Map<String, Expression<*>>,
    spec: Op<Boolean>? = null,
    countQuery: Query = this,
    transform: (ResultRow) -> T,
): Page<T> {
    // copy() чтобы не менять переданные запросы
    val query = copy().apply { spec?.let { andWhere { it } } }
    val count = countQuery.copy().apply { spec?.let { andWhere { it } } }
    val content = query.pageable(pageable, sortColumns).map(transform)

    return PageableExecutionUtils.getPage(content, pageable) { count.count() }
}

/**
 * Pageable -> ORDER BY + LIMIT / OFFSET.
 *
 * Возвращает копию: исходный Query не меняется. Это важно для COUNT — на Query с LIMIT
 * `count()` посчитает только строки страницы.
 *
 * Сортировка — только по явному белому списку [sortColumns]: имя поля в API -> колонка.
 * Колонки можно было бы угадывать из самого запроса (`set.fields`), но это рискованно:
 * API привязывается к именам колонок в БД, в join одно имя (`id`) есть у нескольких таблиц,
 * а клиент получает возможность сортировать по любой колонке, в том числе без индекса.
 */
fun Query.pageable(pageable: Pageable, sortColumns: Map<String, Expression<*>>): Query {
    val orders = pageable.sort.map { order ->
        val column = sortColumns[order.property]
            ?: throw UnsupportedSortException("Unsupported sort property '${order.property}', allowed: ${sortColumns.keys}")
        column to if (order.isAscending) SortOrder.ASC else SortOrder.DESC
    }.toList()

    return copy().apply {
        if (orders.isNotEmpty()) orderBy(*orders.toTypedArray())
        if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset)
    }
}
