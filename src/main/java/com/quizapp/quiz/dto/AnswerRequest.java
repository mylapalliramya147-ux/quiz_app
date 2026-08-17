package com.quizapp.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @Min(value = 1, message = "Question id must be at least 1")
        int questionId,

        @NotBlank(message = "Selected answer must not be blank")
        String selectedAnswer
) {
}
