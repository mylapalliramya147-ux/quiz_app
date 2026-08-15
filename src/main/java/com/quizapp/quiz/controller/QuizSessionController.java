package com.quizapp.quiz.controller;

import com.quizapp.quiz.dto.AnswerRequest;
import com.quizapp.quiz.dto.QuizQuestionResponse;
import com.quizapp.quiz.dto.QuizSessionResponse;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.model.QuizSession;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;
import com.quizapp.quiz.service.QuizSessionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/quiz/sessions")
public class QuizSessionController {

    private final QuizConfigService quizConfigService;
    private final QuestionGenerator questionGenerator;
    private final QuizSessionService quizSessionService;

    public QuizSessionController(QuizConfigService quizConfigService,
                                 QuestionGenerator questionGenerator,
                                 QuizSessionService quizSessionService) {
        this.quizConfigService = quizConfigService;
        this.questionGenerator = questionGenerator;
        this.quizSessionService = quizSessionService;
    }

    @PostMapping
    public QuizSessionResponse startSession() {
        QuizConfig config = quizConfigService.getCurrent();
        QuizSession session = quizSessionService.startSession(questionGenerator.generate(config));
        return toResponse(session);
    }

    @GetMapping("/{sessionId}/questions")
    public QuizSessionResponse getQuestions(@PathVariable String sessionId) {
        return toResponse(quizSessionService.getSession(sessionId));
    }

    @PostMapping("/{sessionId}/answers")
    public AnswerRequest submitAnswer(@PathVariable String sessionId,
                                      @Valid @RequestBody AnswerRequest request) {
        quizSessionService.submitAnswer(sessionId, request.questionId(), request.selectedAnswer());
        return request;
    }

    private QuizSessionResponse toResponse(QuizSession session) {
        List<QuizQuestionResponse> questions = IntStream.range(0, session.questions().size())
                .mapToObj(index -> toQuestionResponse(session.questions(), index))
                .toList();
        return new QuizSessionResponse(session.id(), questions);
    }

    private QuizQuestionResponse toQuestionResponse(List<GeneratedQuestion> questions, int index) {
        GeneratedQuestion question = questions.get(index);
        return new QuizQuestionResponse(index, question.question(), question.options());
    }
}
