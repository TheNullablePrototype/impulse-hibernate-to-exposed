package ru.prototype.meetup.book

import jakarta.validation.Valid
import jakarta.validation.constraints.*

/** Жанр */
enum class Genre { NOVEL, NOVELLA, POETRY, DRAMA, FANTASY }

/**
 * Книга - и в ответах, и в запросах create / update.
 * create: `id == null`, автор - по `author.name`, создаётся, если его нет.
 * update: `id` совпадает с id в пути, меняются только `title` и `genre`.
 */
data class Book(
    val id: Long? = null,
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,
    val genre: Genre,
    @field:Min(0)
    @field:Max(2100)
    val publishedYear: Int,
    @field:Valid
    val author: Author,
)

data class Author(
    val id: Long? = null,
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
)

data class BookImportResult(
    val createdAuthors: Int,
    val createdBooks: Int,
)

/** Импорт: автор указывается по имени и создаётся, если его ещё нет. */
data class BookImportRequest(
    @field:NotEmpty
    @field:Valid
    val books: List<BookImportItem>,
)

data class BookImportItem(
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,
    @field:NotBlank
    @field:Size(max = 255)
    val authorName: String,
    val genre: Genre,
    @field:Min(0)
    @field:Max(2100)
    val publishedYear: Int,
)
