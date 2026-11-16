package ru.prototype.meetup.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.core.PropertyReferenceException
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class ErrorHandler {

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ProblemDetail {
        log.warn { "Handled AppException: [${ex.errorCode}]: ${ex.message}" }

        return ProblemDetail.forStatusAndDetail(ex.status, ex.message).apply {
            setProperty("code", ex.errorCode)
        }
    }

    /** Spring Data JPA: сортировка по полю, которого нет в entity. */
    @ExceptionHandler(PropertyReferenceException::class)
    fun handlePropertyReference(ex: PropertyReferenceException): ProblemDetail =
        handleAppException(UnsupportedSortException(ex.message))
}
