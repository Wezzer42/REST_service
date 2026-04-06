package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.TaskPageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.model.TaskStatus
import reactor.core.publisher.Mono

interface TaskService {
    fun createTask(request: CreateTaskRequest): Mono<TaskResponse>
    fun getTaskById(id: Long): Mono<TaskResponse>
    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<TaskPageResponse>
    fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse>
    fun deleteTask(id: Long): Mono<Void>
}
