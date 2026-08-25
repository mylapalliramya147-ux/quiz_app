package com.quizapp.quiz.service;

import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiQuestionGeneratorTest {

    private static final String MODEL = "gemini-2.5-flash";

    private static final String VALID_JSON = """
            [
              {"question": "What is the capital of Java?", "options": ["A", "B", "C", "D"], "correctAnswer": "A"},
              {"question": "Why study Spring?", "options": ["W", "X", "Y", "Z"], "correctAnswer": "Z"}
            ]
            """;

    private Models models;
    private GenerateContentResponse response;
    private GeminiQuestionGenerator generator;

    @BeforeEach
    void setUp() {
        models = mock(Models.class);
        response = mock(GenerateContentResponse.class);
        when(models.generateContent(any(String.class), any(String.class), any(GenerateContentConfig.class)))
                .thenReturn(response);
        generator = new GeminiQuestionGenerator(provider(models), MODEL, false);
    }

    @Test
    void generatesRequestedNumberOfQuestions() {
        when(response.text()).thenReturn(VALID_JSON);

        List<GeneratedQuestion> questions = generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2));

        assertEquals(2, questions.size());
    }

    @Test
    void parsesQuestionTextOptionsAndCorrectAnswer() {
        when(response.text()).thenReturn(VALID_JSON);

        List<GeneratedQuestion> questions = generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2));

        GeneratedQuestion first = questions.get(0);
        assertEquals("What is the capital of Java?", first.question());
        assertEquals(List.of("A", "B", "C", "D"), first.options());
        assertEquals("A", first.correctAnswer());
    }

    @Test
    void promptContainsTopicDifficultyAndCount() {
        when(response.text()).thenReturn(VALID_JSON);

        generator.generate(new QuizConfig("Spring Boot", Difficulty.HARD, 2));

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(models).generateContent(eq(MODEL), promptCaptor.capture(), any(GenerateContentConfig.class));
        String prompt = promptCaptor.getValue();
        assertTrue(prompt.contains("Spring Boot"));
        assertTrue(prompt.contains("HARD"));
        assertTrue(prompt.contains("exactly 2"));
    }

    @Test
    void configuresJsonResponseSchema() {
        when(response.text()).thenReturn(VALID_JSON);

        generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2));

        ArgumentCaptor<GenerateContentConfig> configCaptor = ArgumentCaptor.forClass(GenerateContentConfig.class);
        verify(models).generateContent(eq(MODEL), any(String.class), configCaptor.capture());
        GenerateContentConfig config = configCaptor.getValue();
        assertEquals(Optional.of("application/json"), config.responseMimeType());
        assertTrue(config.responseSchema().isPresent());
        assertEquals(Optional.of(1), config.candidateCount());
    }

    @Test
    void throwsClearErrorWhenApiKeyNotConfigured() {
        ObjectProvider<Models> emptyProvider = provider(null);
        GeminiQuestionGenerator noKeyGenerator = new GeminiQuestionGenerator(emptyProvider, MODEL, false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> noKeyGenerator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2)));

        assertTrue(exception.getMessage().contains("GEMINI_API_KEY"));
    }

    @Test
    void throwsWhenWrongNumberOfQuestionsReturned() {
        when(response.text()).thenReturn("""
                [{"question": "q1", "options": ["A", "B", "C", "D"], "correctAnswer": "A"}]
                """);

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2)));
    }

    @Test
    void throwsWhenOptionsAreNotExactlyFour() {
        when(response.text()).thenReturn("""
                [{"question": "q1", "options": ["A", "B", "C"], "correctAnswer": "A"}]
                """);

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));
    }

    @Test
    void throwsWhenOptionsAreDuplicated() {
        when(response.text()).thenReturn("""
                [{"question": "q1", "options": ["A", "A", "B", "C"], "correctAnswer": "A"}]
                """);

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));
    }

    @Test
    void throwsWhenCorrectAnswerIsNotOneOfTheOptions() {
        when(response.text()).thenReturn("""
                [{"question": "q1", "options": ["A", "B", "C", "D"], "correctAnswer": "Z"}]
                """);

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));
    }

    @Test
    void throwsWhenResponseIsNotJson() {
        when(response.text()).thenReturn("not json");

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));
    }

    @Test
    void throwsWhenResponseHasNoText() {
        when(response.text()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));
    }

    @Test
    void wrapsApiFailuresWithoutEchoingSdkDetails() {
        when(models.generateContent(any(String.class), any(String.class), any(GenerateContentConfig.class)))
                .thenThrow(new IllegalStateException("400 Bad Request url ...?key=SUPER_SECRET"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> generator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 1)));

        assertEquals("Gemini question generation failed", exception.getMessage());
    }

    @Test
    void throwsWhenForceFallbackIsEnabled() {
        GeminiQuestionGenerator forceFallbackGenerator =
                new GeminiQuestionGenerator(provider(models), MODEL, true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> forceFallbackGenerator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2)));

        assertTrue(exception.getMessage().contains("force-fallback"));
    }

    @Test
    void forceFallbackDoesNotCallGeminiApi() {
        GeminiQuestionGenerator forceFallbackGenerator =
                new GeminiQuestionGenerator(provider(models), MODEL, true);

        assertThrows(IllegalStateException.class,
                () -> forceFallbackGenerator.generate(new QuizConfig("Java", Difficulty.MEDIUM, 2)));

        org.mockito.Mockito.verifyNoInteractions(models);
    }

    private static ObjectProvider<Models> provider(Models models) {
        ObjectProvider<Models> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(models);
        return provider;
    }
}
