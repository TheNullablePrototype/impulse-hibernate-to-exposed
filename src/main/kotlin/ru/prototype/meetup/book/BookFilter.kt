package ru.prototype.meetup.book

import org.jetbrains.exposed.v1.core.*
import org.springframework.data.jpa.domain.Specification
import ru.prototype.meetup.book.exposed.AuthorTable
import ru.prototype.meetup.book.exposed.BookTable
import ru.prototype.meetup.book.jpa.AuthorEntity
import ru.prototype.meetup.book.jpa.BookEntity
import ru.prototype.meetup.common.exposed.iContains
import ru.prototype.meetup.common.exposed.iLike

data class BookFilter(
    val title: String? = null,
    val genres: Set<Genre>? = null,
    val authorName: String? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
) {

    /** JPA Specification. Имена полей обычные строки, опечатка всплывёт только в рантайме. */
    fun toSpecification(): Specification<BookEntity> = Specification.allOf(
        listOfNotNull(
            title?.takeIf(String::isNotBlank)?.let { title ->
                Specification { root, _, cb ->
                    cb.like(cb.lower(root.get("title")), title.toContainsPattern(), LIKE_ESCAPE)
                }
            },
            genres?.takeIf(Set<Genre>::isNotEmpty)?.let { genres ->
                Specification { root, _, _ -> root.get<Genre>("genre").`in`(genres) }
            },
            // Свой join, независимый от @EntityGraph: в SQL будет два join-а к author.
            authorName?.takeIf(String::isNotBlank)?.let { name ->
                Specification { root, _, cb ->
                    val author = root.join<BookEntity, AuthorEntity>("author")
                    cb.like(cb.lower(author.get("name")), name.toContainsPattern(), LIKE_ESCAPE)
                }
            },
            yearFrom?.let { year ->
                Specification { root, _, cb -> cb.greaterThanOrEqualTo(root.get("publishedYear"), year) }
            },
            yearTo?.let { year ->
                Specification { root, _, cb -> cb.lessThanOrEqualTo(root.get("publishedYear"), year) }
            },
        )
    )

    /** Exposed: условие для WHERE. `null` - фильтр пустой, WHERE не нужен. */
    fun toOp(): Op<Boolean>? {
        val conditions = mutableListOf<Op<Boolean>>()
        title?.takeIf(String::isNotBlank)?.let { conditions += BookTable.title iLike it }
        genres?.takeIf(Set<Genre>::isNotEmpty)?.let { conditions += BookTable.genre inList it }
        authorName?.takeIf(String::isNotBlank)?.let { conditions += AuthorTable.name iContains it }
        yearFrom?.let { conditions += BookTable.publishedYear greaterEq it }
        yearTo?.let { conditions += BookTable.publishedYear lessEq it }
        return conditions.reduceOrNull(Op<Boolean>::and)
    }
}

private const val LIKE_ESCAPE = '\\'

/** `%` и `_` из пользовательского ввода ищутся как обычные символы, а не как шаблон. */
private fun String.toContainsPattern(): String {
    val escaped = lowercase()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
    return "%$escaped%"
}
