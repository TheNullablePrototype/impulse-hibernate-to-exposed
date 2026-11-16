package ru.prototype.meetup.book

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {

    @GetMapping("/count")
    fun count(): Long = bookService.count()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): Book = bookService.findById(id)

    @GetMapping
    fun findPage(@PageableDefault(size = 20, sort = ["id"]) pageable: Pageable): PagedModel<Book> =
        PagedModel(bookService.findPage(pageable))

    @PostMapping("/search")
    fun findByFilter(
        @RequestBody filter: BookFilter,
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable,
    ): PagedModel<Book> = PagedModel(bookService.findByFilter(filter, pageable))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody book: Book) = bookService.create(book)

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @Valid @RequestBody book: Book) = bookService.update(id, book)

    @PostMapping("/import")
    fun import(@Valid @RequestBody request: BookImportRequest): BookImportResult = bookService.import(request)

}
