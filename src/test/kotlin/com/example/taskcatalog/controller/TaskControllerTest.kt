package com.example.taskcatalog.controller

import com.example.taskcatalog.dto.CreateTaskRequest
import com.example.taskcatalog.dto.TaskPageResponse
import com.example.taskcatalog.dto.TaskResponse
import com.example.taskcatalog.dto.UpdateTaskStatusRequest
import com.example.taskcatalog.exception.TaskNotFoundException
import com.example.taskcatalog.model.TaskStatus
import com.example.taskcatalog.service.TaskService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebMvcTest(TaskController::class)
@Import(TaskControllerTest.MockConfig::class)
class TaskControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var taskService: TaskService

    @BeforeEach
    fun setup() {
        clearMocks(taskService)
    }

    @Test
    fun `should create task`() {
        val now = LocalDateTime.now()
        val response = TaskResponse(1L, "Task", "Desc", TaskStatus.NEW, now, now)
        val requestBody = CreateTaskRequest(title = "Task", description = "Desc")
        every { taskService.createTask(any()) } returns Mono.just(response)

        performAsync(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Task"))
    }

    @Test
    fun `should get task`() {
        val now = LocalDateTime.now()
        val response = TaskResponse(1L, "Task", null, TaskStatus.NEW, now, now)
        every { taskService.getTaskById(1L) } returns Mono.just(response)

        performAsync(get("/api/tasks/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun `should return 404 when task missing`() {
        every { taskService.getTaskById(999L) } returns Mono.error(TaskNotFoundException(999L))

        performAsync(get("/api/tasks/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should validate request`() {
        val invalidRequest = mapOf("title" to "")

        mockMvc.perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should delete task`() {
        every { taskService.deleteTask(1L) } returns Mono.empty()

        performAsync(delete("/api/tasks/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return paged tasks`() {
        val now = LocalDateTime.now()
        val item = TaskResponse(1L, "Task", null, TaskStatus.NEW, now, now)
        val response = TaskPageResponse(listOf(item), page = 0, size = 1, totalElements = 1, totalPages = 1)
        every { taskService.getTasks(0, 1, TaskStatus.NEW) } returns Mono.just(response)

        performAsync(get("/api/tasks?page=0&size=1&status=NEW"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
    }

    @Test
    fun `should update status`() {
        val now = LocalDateTime.now()
        val response = TaskResponse(1L, "Task", null, TaskStatus.DONE, now, now)
        val requestBody = UpdateTaskStatusRequest(TaskStatus.DONE)
        every { taskService.updateStatus(1L, requestBody) } returns Mono.just(response)

        performAsync(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("DONE"))
    }

    @Test
    fun `should return 400 for negative page`() {
        mockMvc.perform(get("/api/tasks?page=-1&size=5"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 for zero size`() {
        mockMvc.perform(get("/api/tasks?page=0&size=0"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 for invalid status enum`() {
        mockMvc.perform(
            patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"INVALID"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 404 when updating missing task`() {
        every { taskService.updateStatus(999L, any()) } returns Mono.error(TaskNotFoundException(999L))

        performAsync(
            patch("/api/tasks/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateTaskStatusRequest(TaskStatus.DONE)))
        )
            .andExpect(status().isNotFound)
    }

    private fun performAsync(builder: MockHttpServletRequestBuilder): ResultActions {
        val mvcResult = mockMvc.perform(builder)
            .andExpect(request().asyncStarted())
            .andReturn()
        return mockMvc.perform(asyncDispatch(mvcResult))
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun taskService(): TaskService = mockk(relaxed = true)
    }
}
