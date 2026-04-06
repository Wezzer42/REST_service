package com.example.taskcatalog.dto

import com.example.taskcatalog.model.TaskStatus
import jakarta.validation.constraints.NotNull


data class UpdateTaskStatusRequest(
    @field:NotNull
    val status: TaskStatus
)
