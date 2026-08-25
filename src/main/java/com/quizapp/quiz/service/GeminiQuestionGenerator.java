package com.quizapp.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class GeminiQuestionGenerator implements QuestionGenerator {

    private static final String MISSING_KEY_MESSAGE =
            "GEMINI_API_KEY environment variable is not set. Set it to enable AI question generation.";

    private static final String FORCE_FALLBACK_MESSAGE =
            "Gemini AI generation is disabled (app.quiz.ai.force-fallback=true)";

    private final ObjectProvider<Models> modelsProvider;
    private final String modelName;
    private final boolean forceFallback;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiQuestionGenerator(ObjectProvider<Models> modelsProvider,
                                   @Value("${app.quiz.ai.model:gemini-2.5-flash}") String modelName,
                                   @Value("${app.quiz.ai.force-fallback:false}") boolean forceFallback) {
        this.modelsProvider = modelsProvider;
        this.modelName = modelName;
        this.forceFallback = forceFallback;
    }

    @Override
    public List<GeneratedQuestion> generate(QuizConfig config) {
        if (forceFallback) {
            throw new IllegalStateException(FORCE_FALLBACK_MESSAGE);
        }
        Models models = modelsProvider.getIfAvailable();
        if (models == null) {
            throw new IllegalStateException(MISSING_KEY_MESSAGE);
        }
        GenerateContentResponse response;
        try {
            response = models.generateContent(modelName, buildPrompt(config), buildConfig());
        } catch (RuntimeException e) {
            throw new IllegalStateException("Gemini question generation failed", e);
        }
        String text = response.text();
        if (text == null) {
            throw new IllegalStateException("Gemini returned an empty response");
        }
        List<GeminiQuestion> questions;
        try {
            questions = objectMapper.readValue(text, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Gemini returned a response that could not be parsed as JSON", e);
        }
        return toGeneratedQuestions(config, questions);
    }

    private String buildPrompt(QuizConfig config) {
        return "You are an expert quiz question generator. Create exactly " + config.numQuestions()
                + " multiple-choice questions about the topic \"" + config.topic()
                + "\" at difficulty level " + config.difficulty().name() + ".\n\n"
                + "For every question:\n"
                + "- Write a clear, self-contained question statement.\n"
                + "- Provide exactly 4 distinct and plausible answer options.\n"
                + "- Set \"correctAnswer\" to exactly one of the options, using identical text.\n\n"
                + "Respond ONLY with a JSON array of objects in the exact shape:\n"
                + "[{\"question\": \"...\", \"options\": [\"...\", \"...\", \"...\", \"...\"], \"correctAnswer\": \"...\"}]\n"
                + "Do not include any text outside the JSON array.";
    }

    private GenerateContentConfig buildConfig() {
        Schema optionSchema = Schema.builder().type(Type.Known.STRING).build();
        Schema questionSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "question", Schema.builder().type(Type.Known.STRING).build(),
                        "options", Schema.builder().type(Type.Known.ARRAY).items(optionSchema).build(),
                        "correctAnswer", Schema.builder().type(Type.Known.STRING).build()))
                .required(List.of("question", "options", "correctAnswer"))
                .build();
        return GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(Schema.builder().type(Type.Known.ARRAY).items(questionSchema).build())
                .candidateCount(1)
                .build();
    }

    private List<GeneratedQuestion> toGeneratedQuestions(QuizConfig config, List<GeminiQuestion> questions) {
        if (questions.size() != config.numQuestions()) {
            throw new IllegalStateException("Gemini returned " + questions.size()
                    + " questions but " + config.numQuestions() + " were requested");
        }
        for (int i = 0; i < questions.size(); i++) {
            validate(questions.get(i), i);
        }
        return questions.stream()
                .map(question -> new GeneratedQuestion(
                        question.question(), List.copyOf(question.options()), question.correctAnswer()))
                .toList();
    }

    private void validate(GeminiQuestion question, int index) {
        String prefix = "Question " + (index + 1);
        if (question.question() == null || question.question().isBlank()) {
            throw new IllegalStateException(prefix + " has no question text");
        }
        if (question.options() == null || question.options().size() != 4) {
            throw new IllegalStateException(prefix + " must have exactly 4 options");
        }
        if (question.options().stream().anyMatch(option -> option == null || option.isBlank())) {
            throw new IllegalStateException(prefix + " has a blank option");
        }
        if (new LinkedHashSet<>(question.options()).size() != 4) {
            throw new IllegalStateException(prefix + " has duplicate options");
        }
        if (question.correctAnswer() == null || question.correctAnswer().isBlank()) {
            throw new IllegalStateException(prefix + " has no correct answer");
        }
        if (!question.options().contains(question.correctAnswer())) {
            throw new IllegalStateException(prefix + " correct answer is not one of the options");
        }
    }

    private record GeminiQuestion(String question, List<String> options, String correctAnswer) {
    }
}
