package com.quizapp.quiz.dto;

public record QuizResult(
        String sessionId,
        int totalQuestions,
        int answeredQuestions,
        int correctAnswers,
        int wrongAnswers,
        int score,
        double percentage
) {
}
