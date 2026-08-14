package com.quizapp.quiz.service;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.Question;
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

        List<Question> questions = generator.generate(config);

        assertEquals(5, questions.size());
    }

    @Test
    void eachQuestionHasTextOptionsAndContainedCorrectAnswer() {
        QuizConfig config = new QuizConfig("Spring", Difficulty.HARD, 10);

        List<Question> questions = generator.generate(config);

        for (Question question : questions) {
            assertNotNull(question.questionText());
            assertFalse(question.questionText().isBlank());
            assertTrue(question.questionText().contains("Spring"));
            assertNotNull(question.options());
            assertEquals(4, question.options().size());
            assertTrue(question.options().contains(question.correctAnswer()));
        }
    }

    @Test
    void usesDifficultySpecificTemplates() {
        List<Question> easy = generator.generate(new QuizConfig("Java", Difficulty.EASY, 3));
        List<Question> hard = generator.generate(new QuizConfig("Java", Difficulty.HARD, 3));

        assertNotEquals(easy.get(0).questionText(), hard.get(0).questionText());
    }
}
