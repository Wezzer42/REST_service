package com.example.taskcatalog.exception

import com.example.taskcatalog.dto.ErrorResponse
import org.springframework.http.HttpStatus
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
    fun handleTaskNotFound(ex: TaskNotFoundException): ErrorResponse =
        ErrorResponse(
            message = ex.message ?: "Task not found",
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: Exception): ErrorResponse {
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
            timestamp = LocalDateTime.now()
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): ErrorResponse =
        ErrorResponse(
            message = ex.message ?: "Bad request",
            timestamp = LocalDateTime.now()
        )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception): ErrorResponse =
        ErrorResponse(
            message = "Internal server error",
            timestamp = LocalDateTime.now()
        )
}
