package ru.prototype.meetup.book

import org.springframework.http.HttpStatus
import ru.prototype.meetup.common.AppException

class BookNotFoundException(id: Long) : AppException(
    message = "Book not found: id=$id",
    errorCode = "book.not.found",
    status = HttpStatus.NOT_FOUND,
)

class BookIdMustBeNullException(id: Long) : AppException(
    message = "Book id must be null on create, got: $id",
    errorCode = "book.id.must.be.null",
    status = HttpStatus.BAD_REQUEST,
)

class BookIdMismatchException(bodyId: Long?, pathId: Long) : AppException(
    message = "Book id in body ($bodyId) does not match id in path ($pathId)",
    errorCode = "book.id.mismatch",
    status = HttpStatus.BAD_REQUEST,
)
