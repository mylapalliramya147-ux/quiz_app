package com.quizapp.quiz.model;

import java.util.List;

public record Question(String questionText, List<String> options, String correctAnswer) {
}
