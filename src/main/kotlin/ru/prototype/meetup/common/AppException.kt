package ru.prototype.meetup.common

import org.springframework.http.HttpStatus

/** Базовое исключение приложения: сообщение, код для клиента и HTTP-статус. */
open class AppException(
    override val message: String,
    val errorCode: String,
    val status: HttpStatus,
) : RuntimeException(message)

class UnsupportedSortException(message: String) : AppException(
    message = message,
    errorCode = "sort.unsupported",
    status = HttpStatus.BAD_REQUEST,
)
