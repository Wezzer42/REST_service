package com.example.taskcatalog.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val status: Int,
    val path: String,
    val timestamp: LocalDateTime
)
