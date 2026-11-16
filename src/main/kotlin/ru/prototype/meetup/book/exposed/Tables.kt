package ru.prototype.meetup.book.exposed

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import ru.prototype.meetup.book.Genre

object AuthorTable : LongIdTable("author") {
    val name = varchar("name", 255)
}

object BookTable : LongIdTable("book") {
    val title = varchar("title", 255)
    val authorId = reference("author_id", AuthorTable)
    val genre = enumerationByName<Genre>("genre", 32)
    val publishedYear = integer("published_year")
}
