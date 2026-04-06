package com.example.taskcatalog.repository

import com.example.taskcatalog.model.Task
import com.example.taskcatalog.model.TaskStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient
) : TaskRepository {

    override fun save(task: Task): Task {
        val sql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcClient.sql(sql)
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("createdAt", Timestamp.valueOf(task.createdAt))
            .param("updatedAt", Timestamp.valueOf(task.updatedAt))
            .update(keyHolder)

        val generatedId = keyHolder.key?.toLong()
            ?: throw IllegalStateException("Failed to retrieve generated id for task")

        return findById(generatedId)
            ?: throw IllegalStateException("Failed to retrieve task after insert")
    }

    override fun findById(id: Long): Task? {
        val sql = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
            WHERE id = :id
        """.trimIndent()

        return jdbcClient.sql(sql)
            .param("id", id)
            .query(::mapRow)
            .optional()
            .orElse(null)
    }

    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val sql = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
            WHERE (:status IS NULL OR status = :status)
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        return jdbcClient.sql(sql)
            .param("status", status?.name)
            .param("limit", size)
            .param("offset", page * size)
            .query(::mapRow)
            .list()
    }

    override fun countAll(status: TaskStatus?): Long {
        val sql = """
            SELECT COUNT(*) as total
            FROM tasks
            WHERE (:status IS NULL OR status = :status)
        """.trimIndent()

        return jdbcClient.sql(sql)
            .param("status", status?.name)
            .query { rs, _ -> rs.getLong("total") }
            .single()
    }

    override fun updateStatus(id: Long, status: TaskStatus, updatedAt: LocalDateTime): Task? {
        val sql = """
            UPDATE tasks
            SET status = :status,
                updated_at = :updatedAt
            WHERE id = :id
        """.trimIndent()

        val updated = jdbcClient.sql(sql)
            .param("status", status.name)
            .param("updatedAt", Timestamp.valueOf(updatedAt))
            .param("id", id)
            .update()

        if (updated == 0) {
            return null
        }

        return findById(id)
    }

    override fun deleteById(id: Long): Boolean {
        val sql = """
            DELETE FROM tasks
            WHERE id = :id
        """.trimIndent()

        val deleted = jdbcClient.sql(sql)
            .param("id", id)
            .update()

        return deleted > 0
    }

    @Suppress("UNUSED_PARAMETER")
    private fun mapRow(rs: ResultSet, rowNum: Int): Task {
        return Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            status = TaskStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
