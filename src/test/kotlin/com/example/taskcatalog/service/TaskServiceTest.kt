package com.example.taskcatalog.service

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.mapper.toResponse
import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TaskServiceTest {

    private val repository: TaskRepository = mockk()
    private lateinit var service: TaskService

    @BeforeEach
    fun setUp() {
        service = TaskServiceImpl(repository)
    }

    @Test
    fun `should create task`() {
        val now = LocalDateTime.now()
        val created = Task(
            id = 1L,
            title = "Prepare report",
            description = "Monthly report",
            status = TaskStatus.NEW,
            createdAt = now,
            updatedAt = now
        )
        every { repository.save(any()) } returns created

        val request = CreateTaskRequest(title = "Prepare report", description = "Monthly report")

        StepVerifier.create(service.createTask(request))
            .expectNext(created.toResponse())
            .verifyComplete()
    }

    @Test
    fun `should return task by id`() {
        val now = LocalDateTime.now()
        val task = Task(1L, "Task", "Desc", TaskStatus.NEW, now, now)
        every { repository.findById(1L) } returns task

        StepVerifier.create(service.getTaskById(1L))
            .expectNext(task.toResponse())
            .verifyComplete()
    }

    @Test
    fun `should error when task not found`() {
        every { repository.findById(99L) } returns null

        StepVerifier.create(service.getTaskById(99L))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `should return paged result`() {
        val now = LocalDateTime.now()
        val task = Task(1L, "Task", null, TaskStatus.NEW, now, now)
        every { repository.findAll(0, 10, TaskStatus.NEW) } returns listOf(task)
        every { repository.countAll(TaskStatus.NEW) } returns 1

        StepVerifier.create(service.getTasks(0, 10, TaskStatus.NEW))
            .assertNext {
                assertEquals(1, it.content.size)
                assertEquals(1L, it.totalElements)
                assertEquals(1, it.totalPages)
            }
            .verifyComplete()
    }

    @Test
    fun `should update status`() {
        val now = LocalDateTime.now()
        val updated = Task(1L, "Task", null, TaskStatus.DONE, now, now)
        every { repository.updateStatus(1L, TaskStatus.DONE, any()) } returns updated

        StepVerifier.create(service.updateStatus(1L, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .expectNext(updated.toResponse())
            .verifyComplete()
    }

    @Test
    fun `should delete task`() {
        every { repository.deleteById(1L) } returns true

        StepVerifier.create(service.deleteTask(1L))
            .verifyComplete()
    }

    @Test
    fun `should error when delete fails`() {
        every { repository.deleteById(1L) } returns false

        StepVerifier.create(service.deleteTask(1L))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `should validate pagination arguments`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.getTasks(-1, 10, null)
        }

        assertThrows(IllegalArgumentException::class.java) {
            service.getTasks(0, 0, null)
        }

        assertThrows(IllegalArgumentException::class.java) {
            service.getTasks(0, 101, null)
        }
    }

    @Test
    fun `should trim title and normalize blank description`() {
        val now = LocalDateTime.now()
        val created = Task(
            id = 1L,
            title = "Prepare report",
            description = null,
            status = TaskStatus.NEW,
            createdAt = now,
            updatedAt = now
        )

        val slot = slot<Task>()
        every { repository.save(capture(slot)) } returns created

        val request = CreateTaskRequest(
            title = "  Prepare report  ",
            description = "   "
        )

        StepVerifier.create(service.createTask(request))
            .expectNext(created.toResponse())
            .verifyComplete()

        val saved = slot.captured
        assertEquals("Prepare report", saved.title)
        assertEquals(null, saved.description)
        assertEquals(TaskStatus.NEW, saved.status)
    }

    @Test
    fun `should reject too short title after trim`() {
        val request = CreateTaskRequest(
            title = "  ab  ",
            description = null
        )

        StepVerifier.create(service.createTask(request))
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }

    @Test
    fun `should reject blank title after trim`() {
        val request = CreateTaskRequest(
            title = "   ",
            description = null
        )

        StepVerifier.create(service.createTask(request))
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }
}
