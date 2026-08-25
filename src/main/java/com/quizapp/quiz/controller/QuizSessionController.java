package com.quizapp.quiz.controller;

import com.quizapp.quiz.dto.AnswerRequest;
import com.quizapp.quiz.dto.QuizFinishResponse;
import com.quizapp.quiz.dto.QuizQuestionResponse;
import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.dto.QuizSessionResponse;
import com.quizapp.quiz.dto.WrongAnswerReview;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.model.QuizSession;
import com.quizapp.quiz.service.FallbackQuestionGenerator;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;
import com.quizapp.quiz.service.QuizResultPersistenceService;
import com.quizapp.quiz.service.QuizScoreService;
import com.quizapp.quiz.service.QuizSessionService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
@Tag(name="Quiz Session Management", description="Endpoints for managing quiz sessions, submitting answers, and retrieving results")
@RequestMapping("/api/quiz/sessions")
public class QuizSessionController {

    private static final Logger log = LoggerFactory.getLogger(QuizSessionController.class);

    private final QuizConfigService quizConfigService;
    private final QuestionGenerator questionGenerator;
    private final FallbackQuestionGenerator fallbackQuestionGenerator;
    private final QuizSessionService quizSessionService;
    private final QuizScoreService quizScoreService;
    private final QuizResultPersistenceService quizResultPersistenceService;

    private volatile boolean lastSessionUsedFallback;

    public QuizSessionController(QuizConfigService quizConfigService,
                                 QuestionGenerator questionGenerator,
                                 FallbackQuestionGenerator fallbackQuestionGenerator,
                                 QuizSessionService quizSessionService,
                                 QuizScoreService quizScoreService,
                                 QuizResultPersistenceService quizResultPersistenceService) {
        this.quizConfigService = quizConfigService;
        this.questionGenerator = questionGenerator;
        this.fallbackQuestionGenerator = fallbackQuestionGenerator;
        this.quizSessionService = quizSessionService;
        this.quizScoreService = quizScoreService;
        this.quizResultPersistenceService = quizResultPersistenceService;
    }

    @PostMapping
    public QuizSessionResponse startSession() {
        QuizConfig config = quizConfigService.getCurrent();
        boolean usedFallback;
        List<GeneratedQuestion> questions;
        try {
            questions = questionGenerator.generate(config);
            usedFallback = false;
        } catch (IllegalStateException e) {
            log.warn("Primary question generation failed, using fallback: {}", e.getMessage());
            questions = fallbackQuestionGenerator.generate(config);
            usedFallback = true;
        }
        lastSessionUsedFallback = usedFallback;
        QuizSession session = quizSessionService.startSession(questions);
        return toResponse(session, usedFallback);
    }

    @GetMapping("/{sessionId}/questions")
    public QuizSessionResponse getQuestions(@PathVariable String sessionId) {
        return toResponse(quizSessionService.getSession(sessionId), false);
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerRequest submitAnswer(@PathVariable String sessionId,
                                      @Valid @RequestBody AnswerRequest request) {
        quizSessionService.submitAnswer(sessionId, request.questionId() - 1, request.selectedAnswer());
        return request;
    }

    @PostMapping("/{sessionId}/finish")
    public QuizFinishResponse finishSession(@PathVariable String sessionId) {
        QuizSession session = quizSessionService.getSession(sessionId);
        QuizConfig config = quizConfigService.getCurrent();
        QuizResult result = quizScoreService.calculate(session);
        quizResultPersistenceService.save(result, config.topic(), config.difficulty());
        return toFinishResponse(result, session);
    }

    private QuizSessionResponse toResponse(QuizSession session, boolean fallback) {
        List<QuizQuestionResponse> questions = IntStream.range(0, session.questions().size())
                .mapToObj(index -> toQuestionResponse(session.questions(), index))
                .toList();
        return new QuizSessionResponse(session.id(), questions, fallback);
    }

    private QuizQuestionResponse toQuestionResponse(List<GeneratedQuestion> questions, int index) {
        GeneratedQuestion question = questions.get(index);
        return new QuizQuestionResponse(index + 1, question.question(), question.options());
    }

    private QuizFinishResponse toFinishResponse(QuizResult result, QuizSession session) {
        List<WrongAnswerReview> wrongAnswers = buildWrongAnswers(session);
        return new QuizFinishResponse(
                result.sessionId(), result.totalQuestions(), result.answeredQuestions(),
                result.correctAnswers(), result.wrongAnswers(), result.score(),
                result.percentage(), wrongAnswers);
    }

    private List<WrongAnswerReview> buildWrongAnswers(QuizSession session) {
        List<GeneratedQuestion> questions = session.questions();
        Map<Integer, String> answers = session.answers();
        return IntStream.range(0, questions.size())
                .filter(i -> {
                    String submitted = answers.get(i);
                    return submitted == null || !submitted.equals(questions.get(i).correctAnswer());
                })
                .mapToObj(i -> new WrongAnswerReview(
                        i + 1,
                        questions.get(i).question(),
                        answers.getOrDefault(i, "(unanswered)"),
                        questions.get(i).correctAnswer()))
                .toList();
    }
}
