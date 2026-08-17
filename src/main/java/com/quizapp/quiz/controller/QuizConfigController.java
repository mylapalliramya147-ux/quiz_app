package com.quizapp.quiz.controller;

import com.quizapp.quiz.dto.QuizConfigRequest;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.service.QuizConfigService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name="Quiz Configuration", description="Endpoints for managing quiz configuration")
@RequestMapping("/api/quiz/config")
public class QuizConfigController {

    private final QuizConfigService quizConfigService;

    public QuizConfigController(QuizConfigService quizConfigService) {
        this.quizConfigService = quizConfigService;
    }

    @PostMapping
    public QuizConfig saveConfig(@Valid @RequestBody QuizConfigRequest request) {
        return quizConfigService.save(request.toQuizConfig());
    }

    @GetMapping
    public QuizConfig getConfig() {
        return quizConfigService.getCurrent();
    }
}
