package com.quizapp.quiz.service;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockQuestionGeneratorTest {

    private final MockQuestionGenerator generator = new MockQuestionGenerator();

    @Test
    void generatesRequestedNumberOfQuestions() {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 5);

        List<GeneratedQuestion> questions = generator.generate(config);

        assertEquals(5, questions.size());
    }

    @Test
    void eachQuestionHasTextOptionsAndContainedCorrectAnswer() {
        QuizConfig config = new QuizConfig("Spring", Difficulty.HARD, 10);

        List<GeneratedQuestion> questions = generator.generate(config);

        for (GeneratedQuestion question : questions) {
            assertNotNull(question.question());
            assertFalse(question.question().isBlank());
            assertTrue(question.question().contains("Spring"));
            assertNotNull(question.options());
            assertEquals(4, question.options().size());
            assertTrue(question.options().contains(question.correctAnswer()));
        }
    }

    @Test
    void usesDifficultySpecificTemplates() {
        List<GeneratedQuestion> easy = generator.generate(new QuizConfig("Java", Difficulty.EASY, 3));
        List<GeneratedQuestion> hard = generator.generate(new QuizConfig("Java", Difficulty.HARD, 3));

        assertNotEquals(easy.get(0).question(), hard.get(0).question());
    }
}
