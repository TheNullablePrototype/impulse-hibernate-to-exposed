package ru.prototype.meetup.book.jpa

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.prototype.meetup.book.*

@Repository
@ConditionalOnProperty(name = ["meetup.persistence"], havingValue = "jpa", matchIfMissing = true)
@Transactional(readOnly = true)
class JpaBookRepository(
    private val bookRepository: BookEntityRepository,
    private val authorRepository: AuthorEntityRepository,
) : BookRepository {

    override fun count(): Long = bookRepository.count()

    override fun findById(id: Long): Book? =
        bookRepository.findWithAuthorById(id)?.toBook()

    override fun findPage(pageable: Pageable): Page<Book> =
        bookRepository.findAll(pageable).map { it.toBook() }

    override fun findByFilter(filter: BookFilter, pageable: Pageable): Page<Book> =
        bookRepository.findAll(filter.toSpecification(), pageable).map { it.toBook() }

    @Transactional
    override fun create(book: Book) {
        // Автор ищется по имени, нет такого - создаётся.
        val author = authorRepository.findByName(book.author.name)
            ?: authorRepository.save(AuthorEntity(name = book.author.name))

        bookRepository.save(
            BookEntity(
                title = book.title,
                author = author,
                genre = book.genre,
                publishedYear = book.publishedYear,
            )
        )
    }

    @Transactional
    override fun update(id: Long, book: Book) {
        val entity = bookRepository.findByIdOrNull(id) ?: throw BookNotFoundException(id)
        entity.title = book.title
        entity.genre = book.genre
        // save() не нужен: entity managed, UPDATE при коммите сгенерирует dirty checking.
    }

    @Transactional
    override fun import(books: List<BookImportItem>): BookImportResult {
        val names = books.map { it.authorName }.toSet()
        val existing = authorRepository.findAllByNameIn(names)

        val created = authorRepository.saveAll(
            (names - existing.map { it.name }.toSet()).map { AuthorEntity(name = it) }
        )
        val authorsByName = (existing + created).associateBy { it.name }

        bookRepository.saveAll(
            books.map {
                BookEntity(
                    title = it.title,
                    author = authorsByName.getValue(it.authorName),
                    genre = it.genre,
                    publishedYear = it.publishedYear,
                )
            }
        )
        return BookImportResult(createdAuthors = created.size, createdBooks = books.size)
    }
}

private fun BookEntity.toBook() = Book(
    id = id!!,
    title = title,
    genre = genre,
    publishedYear = publishedYear,
    author = Author(id = author.id!!, name = author.name),
)
