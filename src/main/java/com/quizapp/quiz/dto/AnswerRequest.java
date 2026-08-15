package com.quizapp.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @Min(value = 0, message = "Question id must not be negative")
        int questionId,

        @NotBlank(message = "Selected answer must not be blank")
        String selectedAnswer
) {
}
