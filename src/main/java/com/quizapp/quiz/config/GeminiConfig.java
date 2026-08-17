package com.quizapp.quiz.config;

import com.google.genai.Client;
import com.google.genai.Models;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    public static final String API_KEY_ENV_VAR = "GEMINI_API_KEY";

    @Bean
    public Models geminiModels() {
        return buildModels(System.getenv(API_KEY_ENV_VAR));
    }

    static Models buildModels(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return Client.builder().apiKey(apiKey).build().models;
    }
}
