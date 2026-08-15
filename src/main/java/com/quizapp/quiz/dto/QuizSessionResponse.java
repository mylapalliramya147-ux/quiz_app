package com.quizapp.quiz.dto;

import java.util.List;

public record QuizSessionResponse(String sessionId, List<QuizQuestionResponse> questions) {
}
