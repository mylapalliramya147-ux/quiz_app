package com.quizapp.quiz.controller;

import com.quizapp.quiz.model.Question;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/generate")
public class QuestionController {

    private final QuizConfigService quizConfigService;
    private final QuestionGenerator questionGenerator;

    public QuestionController(QuizConfigService quizConfigService, QuestionGenerator questionGenerator) {
        this.quizConfigService = quizConfigService;
        this.questionGenerator = questionGenerator;
    }

    @PostMapping
    public List<Question> generateQuestions() {
        QuizConfig config = quizConfigService.getCurrent();
        return questionGenerator.generate(config);
    }
}
