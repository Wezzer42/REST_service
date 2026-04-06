package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class TaskRepositoryIntegrationTest {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Test
    fun `should save and find task`() {
        val saved = taskRepository.save(buildTask("Task 1", TaskStatus.NEW))

        val found = taskRepository.findById(requireNotNull(saved.id))

        assertNotNull(found)
        assertEquals(saved.id, found?.id)
        assertEquals("Task 1", found?.title)
    }

    @Test
    fun `should fetch paged tasks`() {
        repeat(3) {
            taskRepository.save(
                buildTask(
                    title = "Task $it",
                    status = if (it % 2 == 0) TaskStatus.NEW else TaskStatus.DONE,
                    minuteOffset = it.toLong()
                )
            )
        }

        val tasks = taskRepository.findAll(page = 0, size = 2, status = TaskStatus.NEW)
        val total = taskRepository.countAll(TaskStatus.NEW)

        assertEquals(2, tasks.size)
        assertEquals(2L, total)
        assertTrue(tasks.first().createdAt >= tasks.last().createdAt)
    }

    @Test
    fun `should update status`() {
        val saved = taskRepository.save(buildTask("Task", TaskStatus.NEW))

        val updated = taskRepository.updateStatus(requireNotNull(saved.id), TaskStatus.DONE, LocalDateTime.now())

        assertEquals(TaskStatus.DONE, updated?.status)
    }

    @Test
    fun `should delete task`() {
        val saved = taskRepository.save(buildTask("Task", TaskStatus.NEW))
        val id = requireNotNull(saved.id)

        val deleted = taskRepository.deleteById(id)
        val afterDelete = taskRepository.findById(id)

        assertTrue(deleted)
        assertNull(afterDelete)
    }

    private fun buildTask(title: String, status: TaskStatus, minuteOffset: Long = 0): Task {
        val now = LocalDateTime.now().minusMinutes(minuteOffset)
        return Task(
            id = null,
            title = title,
            description = "description",
            status = status,
            createdAt = now,
            updatedAt = now
        )
    }
}
