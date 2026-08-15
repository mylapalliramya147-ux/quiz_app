package com.quizapp.quiz.service;

import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuizSessionServiceTest {

    private QuizSessionService service;

    private final List<GeneratedQuestion> questions = List.of(
            new GeneratedQuestion("Question 1?", List.of("A", "B", "C", "D"), "A"),
            new GeneratedQuestion("Question 2?", List.of("A", "B", "C", "D"), "B")
    );

    @BeforeEach
    void setUp() {
        service = new QuizSessionService();
    }

    @Test
    void startsSessionWithGivenQuestions() {
        QuizSession session = service.startSession(questions);

        assertNotNull(session.id());
        assertEquals(questions, session.questions());
    }

    @Test
    void getSessionReturnsStoredSession() {
        QuizSession session = service.startSession(questions);

        assertEquals(session, service.getSession(session.id()));
    }

    @Test
    void getSessionThrows404ForUnknownSession() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getSession("unknown"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void submitAnswerStoresValidAnswer() {
        QuizSession session = service.startSession(questions);

        service.submitAnswer(session.id(), 0, "A");

        assertEquals("A", session.answers().get(0));
    }

    @Test
    void submitAnswerThrows400ForInvalidQuestionId() {
        QuizSession session = service.startSession(questions);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submitAnswer(session.id(), 99, "A"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void submitAnswerThrows400WhenAnswerIsNotAnOption() {
        QuizSession session = service.startSession(questions);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submitAnswer(session.id(), 0, "Not an option"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void submitAnswerThrows404ForUnknownSession() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.submitAnswer("unknown", 0, "A"));

        assertEquals(404, ex.getStatusCode().value());
    }
}
