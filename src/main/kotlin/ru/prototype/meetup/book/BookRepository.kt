package ru.prototype.meetup.book

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Контракт слоя данных. Сервис и контроллер знают только про него,
 * Реализация выбирается свойством `meetup.persistence`: `jpa` (по умолчанию) или `exposed`.
 */
interface BookRepository {
    fun count(): Long
    fun findById(id: Long): Book?
    fun findPage(pageable: Pageable): Page<Book>
    fun findByFilter(filter: BookFilter, pageable: Pageable): Page<Book>
    fun create(book: Book)
    fun update(id: Long, book: Book)
    fun import(books: List<BookImportItem>): BookImportResult
}