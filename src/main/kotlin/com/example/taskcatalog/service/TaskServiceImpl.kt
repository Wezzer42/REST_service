package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.TaskPageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.mapper.toResponse
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import org.springframework.stereotype.Service
import kotlin.math.ceil
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository
) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> {
        return Mono.fromCallable {
            val now = LocalDateTime.now()
            val task = Task(
                id = null,
                title = request.title.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                status = TaskStatus.NEW,
                createdAt = now,
                updatedAt = now
            )
            taskRepository.save(task)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.toResponse() }
    }

    override fun getTaskById(id: Long): Mono<TaskResponse> {
        return Mono.fromCallable {
            taskRepository.findById(id) ?: throw TaskNotFoundException(id)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.toResponse() }
    }

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<TaskPageResponse> {
        require(page >= 0) { "page must be greater or equal to 0" }
        require(size > 0) { "size must be greater than 0" }

        return Mono.fromCallable {
            val tasks = taskRepository.findAll(page, size, status)
            val total = taskRepository.countAll(status)
            val totalPages = if (total == 0L) 0 else ceil(total.toDouble() / size).toInt()
            TaskPageResponse(
                content = tasks.map { it.toResponse() },
                page = page,
                size = size,
                totalElements = total,
                totalPages = totalPages
            )
        }.subscribeOn(Schedulers.boundedElastic())
    }

    override fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> {
        return Mono.fromCallable {
            taskRepository.updateStatus(id, request.status, LocalDateTime.now())
                ?: throw TaskNotFoundException(id)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.toResponse() }
    }

    override fun deleteTask(id: Long): Mono<Void> {
        return Mono.fromCallable {
            val removed = taskRepository.deleteById(id)
            if (!removed) {
                throw TaskNotFoundException(id)
            }
        }
            .subscribeOn(Schedulers.boundedElastic())
            .then()
    }
}
