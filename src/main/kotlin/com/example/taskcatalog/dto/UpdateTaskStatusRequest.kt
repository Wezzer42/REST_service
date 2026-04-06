package com.example.taskcatalog.dto

import com.example.taskcatalog.model.TaskStatus


data class UpdateTaskStatusRequest(
    val status: TaskStatus
)
