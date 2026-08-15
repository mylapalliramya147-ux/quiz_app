package com.quizapp.quiz.service;

import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;

import java.util.List;

public interface QuestionGenerator {

    List<GeneratedQuestion> generate(QuizConfig config);
}
