# AGENTS.md

Spring Boot 4.1.0 / Java 21 / Maven project (single module, no CI). Runs only on Windows with the Maven wrapper: `.\mvnw.cmd`.

## Commands
- Run tests: `.\mvnw.cmd test`
- Run app: `.\mvnw.cmd spring-boot:run` (starts on `:8080`)
- Run one test class: `.\mvnw.cmd -Dtest=QuestionControllerTest test`

## Big gotcha: PostgreSQL — only quiz results are persisted
- `spring-boot-starter-data-jpa` + Postgres driver are in `pom.xml`, and `application.properties` points at `jdbc:postgresql://localhost:5432/scoresDB` with a hardcoded password.
- The **only** persisted data is the final `QuizResult`, stored via `QuizResultEntity` + `QuizResultRepository` (Spring Data JPA). Questions, options, and submitted answers stay in memory. `spring.jpa.hibernate.ddl-auto=update` creates/updates the `quiz_results` table.
- JPA autoconfig connects to Postgres at startup, so `QuizApplicationTests` (`@SpringBootTest`) and app startup fail unless Postgres is running on `localhost:5432`.
- The controller/service tests do NOT need the DB: they use `@WebMvcTest` (web layer only) or plain unit tests (repository is mocked). Use those patterns; don't reach for `@SpringBootTest` in new tests unless a full context is actually required.

## API flow (in-memory state, no persistence)
- Config is stored in a `volatile` field in `QuizConfigService` — lost on restart. `POST /api/quiz/generate` returns **404** until a config has been set.
- `POST /api/quiz/config` body: `{"topic": "...", "difficulty": "EASY|MEDIUM|HARD", "numQuestions": n}`. `topic` is `@NotBlank`, `difficulty` is `@NotNull`, `numQuestions` is `@Min(1)` with **no upper bound** — guard against huge values.
- `POST /api/quiz/generate` returns `List<GeneratedQuestion>` **including `correctAnswer`** — this is asserted in the tests, so keep it unless the design changes.
- Request validation lives on the `QuizConfigRequest` record; invalid bodies return 400.

## Quiz sessions (in-memory, no persistence)
- `POST /api/quiz/sessions` starts a session from the current config, generates via `QuestionGenerator`, and returns `{sessionId, questions:[{id, question, options}]}`. The response DTO **strips `correctAnswer`** — assert `.doesNotExist()` when testing this.
- `GET /api/quiz/sessions/{id}/questions` returns the sanitized questions; unknown session id → 404.
- `POST /api/quiz/sessions/{id}/answers` body `{questionId, selectedAnswer}`. `questionId` is the 0-based index into the session's questions; `selectedAnswer` must be one of that question's options (else 400), negative/out-of-range id → 400, unknown session → 404. No score is computed or stored.
- `POST /api/quiz/sessions/{id}/finish` computes a `QuizResult` via `QuizScoreService` (separate from the session service) and **persists it via `QuizResultPersistenceService`** (JPA). It returns `{sessionId, totalQuestions, answeredQuestions, correctAnswers, wrongAnswers, score, percentage}`. **Unanswered questions count as incorrect**: `wrongAnswers = totalQuestions - correctAnswers`, `score = correctAnswers`, `percentage = round(score/total*100, 2)`. The stored `quiz_results` row also carries the current config's `topic`/`difficulty` and a `completedAt` timestamp; questions/options/answers are never stored.
- Sessions + answers live in `QuizSessionService` (`ConcurrentHashMap`), discarded on restart.

## Spring Boot 4 test conventions (differs from Boot 2/3 docs)
- Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito.MockitoBean` — not `@MockBean`.
- Use `@WebMvcTest` from `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` — not `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`.

## Question generation
- `QuestionGenerator` is implemented only by `MockQuestionGenerator` (no real question source). It fills `%s` template placeholders with the config `topic`, cycling 3 templates per `Difficulty`. Adding a `Difficulty` value requires adding a `TEMPLATES` entry or `buildQuestion` NPEs.
- Lombok is declared in the pom but unused; `QuizConfig` and `GeneratedQuestion` are plain records, but `QuizSession` is a mutable in-memory class holding the session's questions and submitted answers.
