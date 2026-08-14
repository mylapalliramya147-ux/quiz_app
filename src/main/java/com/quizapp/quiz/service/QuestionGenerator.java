package com.quizapp.quiz.service;

import com.quizapp.quiz.model.Question;
import com.quizapp.quiz.model.QuizConfig;

import java.util.List;

public interface QuestionGenerator {

    List<Question> generate(QuizConfig config);
}
