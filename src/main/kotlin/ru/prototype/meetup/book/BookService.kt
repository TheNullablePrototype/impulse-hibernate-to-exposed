package ru.prototype.meetup.book

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(private val bookRepository: BookRepository) {

    fun count(): Long = bookRepository.count()

    fun findById(id: Long): Book =
        bookRepository.findById(id) ?: throw BookNotFoundException(id)

    fun findPage(pageable: Pageable): Page<Book> = bookRepository.findPage(pageable)

    fun findByFilter(filter: BookFilter, pageable: Pageable): Page<Book> =
        bookRepository.findByFilter(filter, pageable)

    fun create(book: Book) {
        if (book.id != null) throw BookIdMustBeNullException(book.id)
        bookRepository.create(book)
    }

    fun update(id: Long, book: Book) {
        if (book.id != id) throw BookIdMismatchException(book.id, id)
        bookRepository.update(id, book)
    }

    fun import(request: BookImportRequest): BookImportResult = bookRepository.import(request.books)
}
