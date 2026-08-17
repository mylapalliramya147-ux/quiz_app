package com.quizapp.quiz.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeminiConfigTest {

    @Test
    void returnsNullModelsWhenApiKeyMissingOrBlank() {
        assertNull(GeminiConfig.buildModels(null));
        assertNull(GeminiConfig.buildModels(""));
        assertNull(GeminiConfig.buildModels("   "));
    }

    @Test
    void buildsModelsWhenApiKeyPresent() {
        assertNotNull(GeminiConfig.buildModels("test-api-key"));
    }
}
