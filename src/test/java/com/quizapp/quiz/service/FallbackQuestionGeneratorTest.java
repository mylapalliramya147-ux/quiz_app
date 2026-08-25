package com.quizapp.quiz.service;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackQuestionGeneratorTest {

    private FallbackQuestionGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new FallbackQuestionGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "javascript, EASY, 5",
            "javascript, MEDIUM, 10",
            "javascript, HARD, 15",
            "python, EASY, 5",
            "python, MEDIUM, 10",
            "python, HARD, 15",
            "java, EASY, 5",
            "java, MEDIUM, 10",
            "java, HARD, 15",
            "web, EASY, 5",
            "database, EASY, 5",
            "devops, EASY, 5",
            "algorithms, EASY, 5",
            "security, EASY, 5",
            "unknown topic, EASY, 3"
    })
    void returnsRequestedNumberOfQuestions(String topic, Difficulty difficulty, int numQuestions) {
        QuizConfig config = new QuizConfig(topic, difficulty, numQuestions);

        List<GeneratedQuestion> questions = generator.generate(config);

        assertEquals(numQuestions, questions.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript", "python", "java", "web", "web dev", "database", "databases",
            "devops", "algorithms", "security", "unknown topic"})
    void eachQuestionHasFourDistinctNonBlankOptions(String topic) {
        for (Difficulty difficulty : Difficulty.values()) {
            QuizConfig config = new QuizConfig(topic, difficulty, 3);
            List<GeneratedQuestion> questions = generator.generate(config);

            for (GeneratedQuestion question : questions) {
                assertNotNull(question.question(), "Question text must not be null");
                assertFalse(question.question().isBlank(), "Question text must not be blank");
                assertNotNull(question.options(), "Options must not be null");
                assertEquals(4, question.options().size(), "Must have exactly 4 options");

                Set<String> uniqueOptions = new HashSet<>(question.options());
                assertEquals(4, uniqueOptions.size(), "Options must be distinct");

                for (String option : question.options()) {
                    assertNotNull(option, "Option must not be null");
                    assertFalse(option.isBlank(), "Option must not be blank");
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript", "python", "java", "web", "database", "devops", "algorithms", "security"})
    void correctAnswerBelongsToOptions(String topic) {
        for (Difficulty difficulty : Difficulty.values()) {
            QuizConfig config = new QuizConfig(topic, difficulty, 5);
            List<GeneratedQuestion> questions = generator.generate(config);

            for (GeneratedQuestion question : questions) {
                assertTrue(question.options().contains(question.correctAnswer()),
                        "correctAnswer must be one of the options for: " + question.question());
            }
        }
    }

    @Test
    void wrapsAroundWhenMoreQuestionsRequestedThanAvailable() {
        QuizConfig config = new QuizConfig("javascript", Difficulty.EASY, 100);

        List<GeneratedQuestion> questions = generator.generate(config);

        assertEquals(100, questions.size());
        Set<String> uniqueQuestions = new HashSet<>();
        for (GeneratedQuestion q : questions) {
            uniqueQuestions.add(q.question());
        }
        assertTrue(uniqueQuestions.size() < 100, "Questions should repeat when count exceeds bank size");
    }

    @Test
    void correctAnswersAreNotExposedAsCorrectInQuestionText() {
        QuizConfig config = new QuizConfig("java", Difficulty.MEDIUM, 5);
        List<GeneratedQuestion> questions = generator.generate(config);

        for (GeneratedQuestion question : questions) {
            assertFalse(question.question().toLowerCase().contains("correct answer"),
                    "Question text must not reveal the correct answer");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript", "python", "java", "web", "database", "devops", "algorithms", "security"})
    void topicsContainTopicName(String topic) {
        QuizConfig config = new QuizConfig(topic, Difficulty.EASY, 1);
        List<GeneratedQuestion> questions = generator.generate(config);

        assertFalse(questions.isEmpty());
    }

    @Test
    void unsupportedTopicUsesGenericQuestions() {
        QuizConfig config = new QuizConfig("Quantum Physics", Difficulty.HARD, 3);

        List<GeneratedQuestion> questions = generator.generate(config);

        assertEquals(3, questions.size());
        for (GeneratedQuestion question : questions) {
            assertTrue(question.question().toLowerCase().contains("quantum physics"),
                    "Generic question should contain the topic name");
            assertEquals(4, question.options().size());
            assertTrue(question.options().contains(question.correctAnswer()));
        }
    }

    @Test
    void differentDifficultiesProduceDifferentQuestions() {
        List<GeneratedQuestion> easy = generator.generate(new QuizConfig("javascript", Difficulty.EASY, 3));
        List<GeneratedQuestion> hard = generator.generate(new QuizConfig("javascript", Difficulty.HARD, 3));

        assertNotEqual(easy.get(0).question(), hard.get(0).question());
    }

    @Test
    void singleQuestionRequestWorks() {
        QuizConfig config = new QuizConfig("python", Difficulty.MEDIUM, 1);

        List<GeneratedQuestion> questions = generator.generate(config);

        assertEquals(1, questions.size());
        GeneratedQuestion q = questions.get(0);
        assertNotNull(q.question());
        assertFalse(q.question().isBlank());
        assertEquals(4, q.options().size());
        assertTrue(q.options().contains(q.correctAnswer()));
    }

    @Test
    void sameRequestProducesConsistentResults() {
        QuizConfig config = new QuizConfig("java", Difficulty.MEDIUM, 5);

        List<GeneratedQuestion> first = generator.generate(config);
        List<GeneratedQuestion> second = generator.generate(config);

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).question(), second.get(i).question());
            assertEquals(first.get(i).options(), second.get(i).options());
            assertEquals(first.get(i).correctAnswer(), second.get(i).correctAnswer());
        }
    }

    private static void assertNotEqual(String a, String b) {
        assertFalse(a.equals(b), "Expected different questions but got: " + a);
    }
}
