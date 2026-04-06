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

## Design Decisions
- Repository layer uses `JdbcClient` + native SQL as required.
- Because JDBC is blocking, the service wraps repository calls in `Mono.fromCallable { ... }`
  and shifts them to `Schedulers.boundedElastic()`.
- Spring MVC is used instead of WebFlux end-to-end because the persistence API is blocking.

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

## Manual Verification
### Create task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Prepare report","description":"Monthly financial report"}'
```

### Get task by id
```bash
curl http://localhost:8080/api/tasks/1
```

### List tasks
```bash
curl "http://localhost:8080/api/tasks?page=0&size=10"
```

### Filter by status
```bash
curl "http://localhost:8080/api/tasks?page=0&size=10&status=NEW"
```

### Update status
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DONE"}'
```

### Delete task
```bash
curl -i -X DELETE http://localhost:8080/api/tasks/1
```

## Error Response Example
```json
{
  "message": "Task with id=1 not found",
  "status": 404,
  "path": "/api/tasks/1",
  "timestamp": "2026-04-06T18:01:05"
}
```
