package com.quizapp.quiz.dto;

import java.util.List;

public record QuizFinishResponse(
        String sessionId,
        int totalQuestions,
        int answeredQuestions,
        int correctAnswers,
        int wrongAnswers,
        int score,
        double percentage,
        List<WrongAnswerReview> wrongAnswersList
) {
}
