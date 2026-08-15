package com.quizapp.quiz.service;

import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizSession;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizScoreServiceTest {

    private static final List<String> CORRECT = List.of("A", "B", "C", "D");

    private final QuizScoreService service = new QuizScoreService();

    @Test
    void allCorrectAnswers() {
        QuizResult result = service.calculate(session(3, Map.of(0, "A", 1, "B", 2, "C")));

        assertEquals("session-1", result.sessionId());
        assertEquals(3, result.totalQuestions());
        assertEquals(3, result.answeredQuestions());
        assertEquals(3, result.correctAnswers());
        assertEquals(0, result.wrongAnswers());
        assertEquals(3, result.score());
        assertEquals(100.0, result.percentage(), 0.001);
    }

    @Test
    void allWrongAnswers() {
        QuizResult result = service.calculate(session(3, Map.of(0, "Z", 1, "Z", 2, "Z")));

        assertEquals(3, result.totalQuestions());
        assertEquals(3, result.answeredQuestions());
        assertEquals(0, result.correctAnswers());
        assertEquals(3, result.wrongAnswers());
        assertEquals(0, result.score());
        assertEquals(0.0, result.percentage(), 0.001);
    }

    @Test
    void mixedCorrectAndWrongAnswers() {
        QuizResult result = service.calculate(session(4, Map.of(0, "A", 1, "Z", 2, "C", 3, "Z")));

        assertEquals(4, result.totalQuestions());
        assertEquals(4, result.answeredQuestions());
        assertEquals(2, result.correctAnswers());
        assertEquals(2, result.wrongAnswers());
        assertEquals(2, result.score());
        assertEquals(50.0, result.percentage(), 0.001);
    }

    @Test
    void unansweredQuestionsCountAsIncorrect() {
        QuizResult result = service.calculate(session(3, Map.of(0, "A")));

        assertEquals(3, result.totalQuestions());
        assertEquals(1, result.answeredQuestions());
        assertEquals(1, result.correctAnswers());
        assertEquals(2, result.wrongAnswers());
        assertEquals(1, result.score());
        assertEquals(33.33, result.percentage(), 0.01);
    }

    @Test
    void singleQuestionQuiz() {
        QuizResult result = service.calculate(session(1, Map.of(0, "A")));

        assertEquals(1, result.totalQuestions());
        assertEquals(1, result.answeredQuestions());
        assertEquals(1, result.correctAnswers());
        assertEquals(0, result.wrongAnswers());
        assertEquals(1, result.score());
        assertEquals(100.0, result.percentage(), 0.001);
    }

    @Test
    void maximumTwentyQuestionQuiz() {
        QuizResult result = service.calculate(session(20, Map.of(0, "A", 1, "B", 2, "C", 3, "D")));

        assertEquals(20, result.totalQuestions());
        assertEquals(4, result.answeredQuestions());
        assertEquals(4, result.correctAnswers());
        assertEquals(16, result.wrongAnswers());
        assertEquals(4, result.score());
        assertEquals(20.0, result.percentage(), 0.001);
    }

    @Test
    void percentageCalculation() {
        QuizResult exact = service.calculate(session(4, Map.of(0, "A")));
        assertEquals(25.0, exact.percentage(), 0.001);

        QuizResult fractional = service.calculate(session(7, Map.of(0, "A", 1, "B")));
        assertEquals(28.57, fractional.percentage(), 0.01);
    }

    private QuizSession session(int numQuestions, Map<Integer, String> answers) {
        List<GeneratedQuestion> questions = IntStream.range(0, numQuestions)
                .mapToObj(i -> new GeneratedQuestion("Question " + (i + 1) + "?",
                        List.of("A", "B", "C", "D"), CORRECT.get(i % CORRECT.size())))
                .toList();
        QuizSession session = new QuizSession("session-1", questions);
        answers.forEach(session::submitAnswer);
        return session;
    }
}
