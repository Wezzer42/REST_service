package com.example.taskcatalog.exception

import com.example.taskcatalog.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTaskNotFound(
        ex: TaskNotFoundException,
        request: HttpServletRequest
    ): ErrorResponse =
        ErrorResponse(
            message = ex.message ?: "Task not found",
            status = HttpStatus.NOT_FOUND.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(
        ex: Exception,
        request: HttpServletRequest
    ): ErrorResponse {
        val bindingResult = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult
            is BindException -> ex.bindingResult
            else -> throw IllegalArgumentException("Unsupported exception type")
        }

        val message = bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation failed" }

        return ErrorResponse(
            message = message,
            status = HttpStatus.BAD_REQUEST.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ErrorResponse =
        ErrorResponse(
            message = ex.constraintViolations.joinToString("; ") { violation ->
                "${violation.propertyPath}: ${violation.message}"
            },
            status = HttpStatus.BAD_REQUEST.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnreadableMessage(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ErrorResponse =
        ErrorResponse(
            message = "Malformed request body or invalid enum value",
            status = HttpStatus.BAD_REQUEST.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ErrorResponse =
        ErrorResponse(
            message = ex.message ?: "Bad request",
            status = HttpStatus.BAD_REQUEST.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ErrorResponse =
        ErrorResponse(
            message = "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
}
