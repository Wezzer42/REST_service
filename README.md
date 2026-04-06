# Task Catalog Service

REST API for managing tasks with CRUD operations, pagination, filtering, and error handling. Built with Kotlin and Spring Boot using JdbcClient and Reactor on the service layer.

## Tech Stack
- Kotlin 2.3
- Spring Boot 3.5 (MVC)
- Reactor (Mono in service layer)
- JdbcClient + native SQL
- Flyway + H2
- MockMvc / MockK / JUnit 5

## Architecture
- Blocking JDBC repository using `JdbcClient` and raw SQL.
- Service layer exposes `Mono` APIs and shifts blocking calls to `Schedulers.boundedElastic()`.
- Controllers are MVC endpoints returning `Mono` results.
- Centralized `@RestControllerAdvice` for consistent error payloads.

## Getting Started
```bash
./gradlew bootRun
```
Application runs on `http://localhost:8080` with in-memory H2 database (`MODE=PostgreSQL`). Flyway applies schema on startup.

### Running Tests
```bash
./gradlew test
```

## API Endpoints
- `POST /api/tasks` – create task
- `GET /api/tasks?page={page}&size={size}&status={status?}` – list tasks with pagination/filter
- `GET /api/tasks/{id}` – get task by id
- `PATCH /api/tasks/{id}/status` – update status
- `DELETE /api/tasks/{id}` – delete task

## Sample Payloads
**Create**
```json
{
  "title": "Prepare report",
  "description": "Monthly financial report"
}
```

**Update Status**
```json
{
  "status": "DONE"
}
```
