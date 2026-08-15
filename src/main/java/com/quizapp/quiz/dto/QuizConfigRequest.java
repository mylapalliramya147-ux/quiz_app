package com.quizapp.quiz.dto;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.QuizConfig;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizConfigRequest(
        @NotBlank(message = "Topic must not be blank")
        String topic,

        @NotNull(message = "Difficulty must be EASY, MEDIUM, or HARD")
        Difficulty difficulty,

        @Min(value = 1, message = "Number of questions must be at least 1")
        @Max(value = 20, message = "Number of questions must not exceed 20")
        int numQuestions
) {

    public QuizConfig toQuizConfig() {
        return new QuizConfig(topic, difficulty, numQuestions);
    }
}
