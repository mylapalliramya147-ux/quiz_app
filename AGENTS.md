# AGENTS.md

Spring Boot 4.1.0 / Java 21 / Maven project (single module, no CI). Runs only on Windows with the Maven wrapper: `.\mvnw.cmd`.

## Commands
- Run tests: `.\mvnw.cmd test`
- Run app: `.\mvnw.cmd spring-boot:run` (starts on `:8080`)
- Run one test class: `.\mvnw.cmd -Dtest=QuestionControllerTest test`

## Big gotcha: the DB is configured but never used
- `spring-boot-starter-data-jpa` + Postgres driver are in `pom.xml`, and `application.properties` points at `jdbc:postgresql://localhost:5432/scoresDB` with a hardcoded password.
- There are **no entities or repositories** (`repository/` is just a `.gitkeep`). JPA autoconfig connects to Postgres at startup anyway, so `QuizApplicationTests` (`@SpringBootTest`) and app startup fail unless Postgres is running on `localhost:5432`.
- The controller/service tests do NOT need the DB: they use `@WebMvcTest` (web layer only) or plain unit tests. Use those patterns; don't reach for `@SpringBootTest` in new tests unless a full context is actually required.

## API flow (in-memory state, no persistence)
- Config is stored in a `volatile` field in `QuizConfigService` — lost on restart. `POST /api/quiz/generate` returns **404** until a config has been set.
- `POST /api/quiz/config` body: `{"topic": "...", "difficulty": "EASY|MEDIUM|HARD", "numQuestions": n}`. `topic` is `@NotBlank`, `difficulty` is `@NotNull`, `numQuestions` is `@Min(1)` with **no upper bound** — guard against huge values.
- `POST /api/quiz/generate` returns `List<Question>` **including `correctAnswer`** — this is asserted in the tests, so keep it unless the design changes.
- Request validation lives on the `QuizConfigRequest` record; invalid bodies return 400.

## Spring Boot 4 test conventions (differs from Boot 2/3 docs)
- Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito.MockitoBean` — not `@MockBean`.
- Use `@WebMvcTest` from `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` — not `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`.

## Question generation
- `QuestionGenerator` is implemented only by `MockQuestionGenerator` (no real question source). It fills `%s` template placeholders with the config `topic`, cycling 3 templates per `Difficulty`. Adding a `Difficulty` value requires adding a `TEMPLATES` entry or `buildQuestion` NPEs.
- Lombok is declared in the pom but unused; models are plain records.
