package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import java.time.LocalDateTime

interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: Long): Task?
    fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task>
    fun countAll(status: TaskStatus?): Long
    fun updateStatus(id: Long, status: TaskStatus, updatedAt: LocalDateTime): Task?
    fun deleteById(id: Long): Boolean
}
