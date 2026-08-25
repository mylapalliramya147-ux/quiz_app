package com.quizapp.quiz.dto;

public record WrongAnswerReview(int questionNumber, String question, String yourAnswer, String correctAnswer) {
}
