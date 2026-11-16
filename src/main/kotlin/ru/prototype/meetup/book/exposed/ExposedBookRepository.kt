package ru.prototype.meetup.book.exposed

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.prototype.meetup.book.*
import ru.prototype.meetup.common.exposed.toPage

@Repository
@ConditionalOnProperty(name = ["meetup.persistence"], havingValue = "exposed")
@Transactional(transactionManager = "springTransactionManager", readOnly = true)
class ExposedBookRepository : BookRepository {

    override fun count(): Long = BookTable.selectAll().count()

    override fun findById(id: Long): Book? =
        booksWithAuthor()
            .selectAll()
            .where { BookTable.id eq id }
            .singleOrNull()?.toBook()

    override fun findPage(pageable: Pageable): Page<Book> =
        booksWithAuthor()
            .selectAll()
            // author_id not null + FK: число книг не зависит от join, считаем без него
            .toPage(pageable, SORT_COLUMNS, countQuery = BookTable.selectAll()) { it.toBook() }

    override fun findByFilter(filter: BookFilter, pageable: Pageable): Page<Book> =
        booksWithAuthor()
            .selectAll()
            .toPage(pageable, SORT_COLUMNS, filter.toOp()) { it.toBook() }

    @Transactional(transactionManager = "springTransactionManager")
    override fun create(book: Book) {
        // Автор ищется по имени, нет такого - создаётся.
        val authorId = AuthorTable
            .select(AuthorTable.id)
            .where { AuthorTable.name eq book.author.name }
            .singleOrNull()?.get(AuthorTable.id)
            ?: AuthorTable.insertAndGetId { it[name] = book.author.name }

        BookTable.insert {
            it[title] = book.title
            it[BookTable.authorId] = authorId
            it[genre] = book.genre
            it[publishedYear] = book.publishedYear
        }
    }

    @Transactional(transactionManager = "springTransactionManager")
    override fun update(id: Long, book: Book) {
        val updated = BookTable.update({ BookTable.id eq id }) {
            it[title] = book.title
            it[genre] = book.genre
        }
        if (updated == 0) throw BookNotFoundException(id)
    }

    @Transactional(transactionManager = "springTransactionManager")
    override fun import(books: List<BookImportItem>): BookImportResult {
        val names = books.map { it.authorName }.toSet()
        val existing = AuthorTable.selectAll()
            .where { AuthorTable.name inList names }
            .associate { it[AuthorTable.name] to it[AuthorTable.id] }

        // Один batch на всех новых авторов, сгенерированные id возвращаются сразу.
        val created = AuthorTable.batchInsert(names - existing.keys) { name ->
            this[AuthorTable.name] = name
        }.associate { it[AuthorTable.name] to it[AuthorTable.id] }

        val authorIds = existing + created

        // Один batch на все книги. id книг не нужны - JDBC-драйвер склеит вставку в один INSERT.
        BookTable.batchInsert(books, shouldReturnGeneratedValues = false) { book ->
            this[BookTable.title] = book.title
            this[BookTable.authorId] = authorIds.getValue(book.authorName)
            this[BookTable.genre] = book.genre
            this[BookTable.publishedYear] = book.publishedYear
        }

        return BookImportResult(createdAuthors = created.size, createdBooks = books.size)
    }

    companion object {
        private val SORT_COLUMNS: Map<String, Expression<*>> = mapOf(
            "id" to BookTable.id,
            "author.id" to AuthorTable.id,
            "title" to BookTable.title,
            "publishedYear" to BookTable.publishedYear,
        )

        /** FROM book INNER JOIN author ON book.author_id = author.id */
        private fun booksWithAuthor(): Join = BookTable.join(
            AuthorTable,
            JoinType.INNER,
            onColumn = BookTable.authorId,
            otherColumn = AuthorTable.id,
        )
    }

}

// ---------- Маппинг ResultRow -> DTO ----------

private fun ResultRow.toBook() = Book(
    id = this[BookTable.id].value,
    title = this[BookTable.title],
    genre = this[BookTable.genre],
    publishedYear = this[BookTable.publishedYear],
    author = toAuthor(),
)

private fun ResultRow.toAuthor() = Author(
    id = this[AuthorTable.id].value,
    name = this[AuthorTable.name],
)
