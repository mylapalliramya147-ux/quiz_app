package com.quizapp.quiz.controller;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.service.QuizConfigService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizConfigController.class)
class QuizConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizConfigService quizConfigService;

    @Test
    void shouldAcceptValidQuizConfig() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 5);
        when(quizConfigService.save(any(QuizConfig.class))).thenReturn(config);

        mockMvc.perform(post("/api/quiz/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "topic": "Java",
                                  "difficulty": "MEDIUM",
                                  "numQuestions": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("Java"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.numQuestions").value(5));
    }

    @Test
    void shouldRejectNonPositiveQuestionCount() throws Exception {
        mockMvc.perform(post("/api/quiz/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "topic": "Java",
                                  "difficulty": "EASY",
                                  "numQuestions": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankTopic() throws Exception {
        mockMvc.perform(post("/api/quiz/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "topic": " ",
                                  "difficulty": "HARD",
                                  "numQuestions": 10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStoredConfig() throws Exception {
        QuizConfig config = new QuizConfig("History", Difficulty.HARD, 8);
        when(quizConfigService.getCurrent()).thenReturn(config);

        mockMvc.perform(get("/api/quiz/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("History"))
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.numQuestions").value(8));
    }
}
