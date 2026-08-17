# AGENTS.md

Spring Boot 4.1.0 / Java 21 / Maven project (single module, no CI). Runs only on Windows with the Maven wrapper: `.\mvnw.cmd`.

## Security: no secrets in git
- Never commit, push, or expose passwords, API keys, DB credentials, tokens, or other secrets. Use environment variables (e.g. `${DB_PASSWORD}`) or a local, `.gitignore`-ignored config file for sensitive values.
- **Current known issue:** `src/main/resources/application.properties` still hardcodes the Postgres password (`7979`) and it is present in git history. Do NOT touch it unless the user explicitly asks; if the user asks to fix it, recommend env-var placeholder + rotation + history scrub (BFG / `git filter-repo`) and coordinate before rewriting history.

## Gemini API key (AI question generation)
- The Google Gemini API key must NEVER be hard-coded in source code, `application.properties`, tests, `AGENTS.md`, or Git. Always read it from the `GEMINI_API_KEY` environment variable.
- Never print, log, expose, or commit the key. Treat it like any secret: no logging, no request dumps, no error messages that echo it.

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
- `POST /api/quiz/sessions/{id}/answers` body `{questionId, selectedAnswer}`. `questionId` is the **1-based** question ID returned by the session endpoints (1 = first question); `selectedAnswer` must be one of that question's options (else 400), negative/out-of-range id → 400, unknown session → 404. No score is computed or stored.
- `POST /api/quiz/sessions/{id}/finish` computes a `QuizResult` via `QuizScoreService` (separate from the session service) and **persists it via `QuizResultPersistenceService`** (JPA). It returns `{sessionId, totalQuestions, answeredQuestions, correctAnswers, wrongAnswers, score, percentage}`. **Unanswered questions count as incorrect**: `wrongAnswers = totalQuestions - correctAnswers`, `score = correctAnswers`, `percentage = round(score/total*100, 2)`. The stored `quiz_results` row also carries the current config's `topic`/`difficulty` and a `completedAt` timestamp; questions/options/answers are never stored.
- Sessions + answers live in `QuizSessionService` (`ConcurrentHashMap`), discarded on restart.

## Spring Boot 4 test conventions (differs from Boot 2/3 docs)
- Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito.MockitoBean` — not `@MockBean`.
- Use `@WebMvcTest` from `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` — not `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`.

## Question generation
- `QuestionGenerator` has two `@Service` implementations: `GeminiQuestionGenerator` (`@Primary`, the real one) and `MockQuestionGenerator` (kept as a fallback/test helper).
- `GeminiQuestionGenerator` uses the official GenAI Java SDK (`com.google.genai:google-genai`). The API key is read ONLY from the `GEMINI_API_KEY` env var, via the `geminiModels()` `@Bean` in `GeminiConfig`; that bean returns `null` when the key is missing so the app still starts (full tests must pass without the key). The generator injects `ObjectProvider<Models>` and throws a clear `IllegalStateException` when no key is configured.
- The model name comes from `app.quiz.ai.model` in `application.properties` (never the key). The generator requests `application/json` with a `responseSchema` (array of `{question, options[4], correctAnswer}`) and validates the parsed output: exact question count, exactly 4 non-blank distinct options, and `correctAnswer` must equal one of the options.
- Do NOT echo SDK error messages (they can contain the API key in the request URL) — the generator wraps failures with a generic message and keeps the cause.
- `MockQuestionGenerator` fills `%s` template placeholders with the config `topic`, cycling 3 templates per `Difficulty`. Adding a `Difficulty` value requires adding a `TEMPLATES` entry or `buildQuestion` NPEs.
- Tests never call the real Gemini API: `GeminiQuestionGeneratorTest` mocks `Models`/`GenerateContentResponse`; `GeminiConfigTest` verifies the key-missing → null bean path.
- Lombok is declared in the pom but unused; `QuizConfig` and `GeneratedQuestion` are plain records, but `QuizSession` is a mutable in-memory class holding the session's questions and submitted answers.

## Frontend (vanilla JS SPA in `src/main/resources/static/`)
- **No build step.** Pure vanilla JS (ES modules), HTML, and CSS served directly by Spring Boot from `static/`. No npm, no Node, no bundler.
- **Files:** `index.html` (app shell), `css/styles.css` (design system), `js/app.js` (hash router), `js/state.js` (client-side state), `js/api.js` (fetch wrapper), `js/pages/*.js` (6 pages), `js/components/*.js` (loader, toast).
- **Router:** Hash-based (`#welcome`, `#topic`, `#difficulty`, `#num-questions`, `#quiz`, `#result`). `app.js` listens for `hashchange` and renders the matching page into `#page-container`.
- **API client (`api.js`):** Configurable base URL (`window.location.origin`). Calls: `POST /api/quiz/config`, `POST /api/quiz/sessions`, `POST /api/quiz/sessions/{id}/answers`, `POST /api/quiz/sessions/{id}/finish`. Never exposes API keys.
- **State (`state.js`):** Player name, topic, difficulty, numQuestions, session data, questions, answers, result. Reset clears everything. Play Again preserves name + topic.
- **Topic list is frontend-defined** (8 topics: javascript, python, java, web, database, devops, algorithms, security). The backend accepts any string via `QuizConfigRequest.topic`.
- **Question IDs are 1-based** in the UI (matches backend response). `api.js` sends 1-based IDs directly — the backend controller converts to 0-based internally.
- **Loading states:** Spinner + message while AI generates questions. Toast notifications for API errors.
- **CSS design system:** CSS custom properties for theming, 3D flip cards (topic selection), selection cards (difficulty, num-questions), animated score ring (results), responsive grid layouts.
- **Accessibility:** `role`, `aria-label`, `aria-pressed`, `aria-checked`, `:focus-visible`, `prefers-reduced-motion` support.
- **Backend is untouched.** All existing tests (53/53) pass unchanged.
