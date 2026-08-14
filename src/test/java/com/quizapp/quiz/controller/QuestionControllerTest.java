package com.quizapp.quiz.controller;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.Question;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizConfigService quizConfigService;

    @MockitoBean
    private QuestionGenerator questionGenerator;

    @Test
    void shouldGenerateQuestionsForStoredConfig() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 2);
        Question first = new Question("Which statement best describes Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option A");
        Question second = new Question("Why do people study Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option C");

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(questionGenerator.generate(config)).thenReturn(List.of(first, second));

        mockMvc.perform(post("/api/quiz/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].questionText").value("Which statement best describes Java?"))
                .andExpect(jsonPath("$[0].correctAnswer").value("Option A"));

        verify(questionGenerator).generate(config);
    }

    @Test
    void shouldReturn404WhenNoConfigStored() throws Exception {
        when(quizConfigService.getCurrent())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz configuration has been set yet"));

        mockMvc.perform(post("/api/quiz/generate"))
                .andExpect(status().isNotFound());
    }
}
