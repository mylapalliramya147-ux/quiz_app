package com.quizapp.quiz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.quiz.ai.force-fallback=true")
class ForceFallbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startSessionUsesFallbackWhenForceFallbackIsTrue() throws Exception {
        mockMvc.perform(post("/api/quiz/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "topic": "Java",
                                  "difficulty": "MEDIUM",
                                  "numQuestions": 2
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/quiz/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallback").value(true))
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.questions[1].correctAnswer").doesNotExist());
    }
}
